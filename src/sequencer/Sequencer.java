package sequencer;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Comparable;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.lang.Runnable;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * The main class computing the final consensus
 * @author Guillaume Huysmans, Jason Bury
 */
public class Sequencer{
	/**
	 * Enter the path of the fragments file as first parameter
	 */
	public static void main(String args[]){
		if(args.length < 1){
			System.err.println("usage: java -jar sequencer.jar collection3.fasta");
			System.exit(1);
		}
		else{
			try {
				System.out.print("Loading fragments...");
				List<Sequence> fragments = load(args[0]);
				System.out.println(" loaded "+fragments.size()+" fragments.");
				System.out.println("Computing edges...");
				List<Edge> edges = allEdges(fragments, 2);
				System.out.println("Computing the Hamiltonian path...");
				List<Edge> path = hamiltonian(edges, fragments.size());
				char num = args[0].substring(args[0].length()-7).charAt(0);
				System.out.println("Computing the consensus...");
				String consensus = getConsensus(fragments, path);
				//FIXME what is our group number?
				save(consensus, "HUYSMANS-BURYcollection"+num, num, "1");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Compute a part of all edges and their weight
	 * @param divisor How much the work is divided. It computes 1/divisor of all paths
	 * @param part What part of the work to compute. Indexed from 0
	 * @param fragments
	 * @param edges The list to add edges
	 */
	private static void computeEdges(int divisor, int part, List<Sequence> fragments, ArrayList<Edge> edges){
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		long startTime = bean.getCurrentThreadCpuTime();
		int i,j;
		int N = fragments.size();
		for(i=part; i<N ; i+=divisor){
			for(j=i+1 ; j<N ; j++){
				//i is f, j is g. i+N is f', j+N is g'
				short[] aps = fragments.get(i).getAlignmentScore(fragments.get(j));
				edges.add(new Edge( i, j, aps[0] ));//add {f,g}
				edges.add(new Edge( j, i, aps[1] ));//add {g,f}
				edges.add(new Edge( j+N, i+N, aps[0] ));//add {g',f'}
				edges.add(new Edge( i+N, j+N, aps[1] ));//add {f',g'}
				aps = fragments.get(i).getAlignmentScore(fragments.get(j).getComplementary());
				edges.add(new Edge( i, j+N, aps[0] ));//add {f,g'}
				edges.add(new Edge( j+N, i, aps[1] ));//add {g',f}
				edges.add(new Edge( j, i+N, aps[0] ));//add {g,f'}
				edges.add(new Edge( i+N, j, aps[1] ));//add {f',g}
			}
		}
		//System.err.println(part+","+(bean.getCurrentThreadCpuTime()-startTime)/1000000);
	}

	/**
	 * Compute all AlignmentPath
	 * @param fragments The list of Sequence, like returned by the load() method
	 * @param nThreads The number of threads to use.
	 * @return A list of all edges between all pairs of Sequence in fragments and their reverted complementary.
	 * If the length of fragments is N, then N+i is the reverted complementary of the ith fragment. Indexed from 0.
	 */
	public static List<Edge> allEdges(List<Sequence> fragments, int nThreads){//TODO Paralelize
		ArrayList<Edge> edges = new ArrayList<Edge>(4*((fragments.size()*fragments.size()) - fragments.size()));
		//build all edges
		if(nThreads == 1){
			computeEdges(nThreads, 0, fragments, edges);
		}
		else{
			Thread[] threads = new Thread[nThreads-1];
			int i;
			//Create and launch remaining threads
			for(i=0 ; i<nThreads-1 ; i++){
				threads[i] = new Thread(new EdgeComputer(fragments, edges, nThreads, i+1));//Constructor of Thread can take a name as parameter
				threads[i].start();
			}
			computeEdges(nThreads, 0, fragments, edges);
			//And wait they finish
			try{
				for(i=0 ; i<nThreads-1 ; i++){
					threads[i].join();
				}
			} catch(InterruptedException e){
				System.out.println("interrupted");
				for(i=0 ; i<nThreads-1 ; i++){
					threads[i].interrupt();
				}
				Thread.currentThread().interrupt();
			}
		}
		return edges;
	}

	/**
	 * Compute the Hamiltonian path
	 * @param edges The list of all edges
	 * @param nSequences The number of sequences (not including reverted complementary)
	 * @return The entire ordered Hamiltonian path.
	 */
	public static List<Edge> hamiltonian(List<Edge> edges, int nSequences){
		//init tables
		int totalN = 2*nSequences;
		boolean[] in = new boolean[totalN];
		boolean[] out = new boolean[totalN];
		Edge[] nextEdges = new Edge[totalN];//next[i] is the edge from i in the hamiltonian path
		int i;
		for(i=0 ; i<totalN ; i++){
			in[i] = false;
			out[i] = false;
		}
		UnionFind uf = new UnionFind(totalN);
		//start greedy
		Comparator<Edge> comparator = Collections.reverseOrder();
		Collections.sort(edges, comparator);
		for(Edge e : edges){
			if(	!in[e.to] && !out[e.from]){
				int rootFrom = uf.find(e.from);
				int rootTo = uf.find(e.to);
				if( rootFrom != rootTo){
					in[e.to]=true;
					out[e.from]=true;
					nextEdges[e.from]=e;
					//if the in and out of their complementary is true, they will never be taken
					int compIndex;
					if(e.from < nSequences)
						compIndex = e.from + nSequences;
					else
						compIndex = e.from - nSequences;
					in[compIndex] = true;
					out[compIndex] = true;
					uf.ignore(compIndex);
					if(e.to < nSequences)
						compIndex = e.to + nSequences;
					else
						compIndex = e.to - nSequences;
					in[compIndex] = true;
					out[compIndex] = true;
					uf.ignore(compIndex);
					uf.directUnion(rootFrom, rootTo);
				}
			}
			if(uf.getNsets() == 1){
				break;
			}
		}
		//find the first node
		int start=-1;
		for(i=0 ; i<totalN ; i++){
			if(in[i]==false){
				start = i;
				break;
			}
		}
		//build the path. Ordered this time
		i=start;
		ArrayList<Edge> hamiltonian = new ArrayList<Edge>( nSequences-1);
		while(out[i]){//while we don't reach the end of the path
			hamiltonian.add(nextEdges[i]);
			i = nextEdges[i].to;
		}
		return hamiltonian;
	}

	/**
	 * save a String in a fasta file
	 * @param sequence The string to save
	 * @param filename The name of the file without the '.fasta' sufix.
	 * @param numCollection The number of the collection
	 * @param numGroup The number of our group
	 */
	public static void save(String sequence, String filename, char numCollection, String numGroup)
			throws FileNotFoundException, IOException {
		PrintWriter output = new PrintWriter(filename+".fasta");
		int len = sequence.length();
		output.println(">Groupe-"+numGroup+" Collection "+numCollection+" Longueur "+Integer.toString(len));
		int i;
		for (i=80; i<len; i+=80)
			//we have at least 80 characters to print
			output.println(sequence.substring(i-80, i));
		//print the rest (< 80 characters)
		output.println(sequence.substring(i-80));
		output.close();
	}

	/**
	 * Load a fasta file. The inverted complementary of these fragments aren't
	 * included but you can get them using #Sequence.getComplementary().
	 * @param name The path of the file
	 * @return A list of loaded fragments.
	 */
	public static List<Sequence> load(String name)
			throws FileNotFoundException, IOException {
		List<Sequence> fragments = new ArrayList<Sequence>();
		BufferedReader input = new BufferedReader(new FileReader(name));
		StringBuilder current_sequence = new StringBuilder();
		int c;
		do {
			c = input.read();
			if (c==-1 || c == '>') {
				if (current_sequence.length() > 0) {
					fragments.add(new Sequence(current_sequence.toString()));
					current_sequence = new StringBuilder();
				}
				if (c == '>')
					input.readLine(); //trash it
			}
			else {
				current_sequence.append((char)c);
				current_sequence.append(input.readLine());
			}
		} while (c != -1);
		return fragments;
	}

	/**
	 * Compute a consensus
	 * @param fragments The list of fragments
	 * @param path A Hamiltonian path
	 * @return A possible final consensus
	 */
	public static String getConsensus(List<Sequence> frags, List<Edge> path){
		StringBuilder ret = new StringBuilder();
		Alignment al = null;
		Iterator<Edge> it = path.iterator();
		PriorityQueue<Integer> pq = new PriorityQueue<>(); //triggers votes
		Set<Alignment> rem = new HashSet<>(); //we have to move pointers once!
		int n = frags.size();
		int pos = 0;
		do {
			Edge edge = null;
			boolean go = true;
			if (it.hasNext()) {
				edge = it.next();
				Sequence a, b;
				if (edge.from > n)
					a = frags.get(edge.from - n).getComplementary();
				else
					a = frags.get(edge.from);
				if (edge.to > n)
					b = frags.get(edge.to - n).getComplementary();
				else
					b = frags.get(edge.to);
				if (al == null) {
					//let's not forget the first sequence
					al = new Alignment(a.toString(), 0, 0);
					pq.add(al.endsAt - 1);
					rem.add(al);
				}
				al = getAlignment(al.aligned, a, b, pos);
				pq.add(pos);
				pq.add(al.endsAt - 1);
				rem.add(al);
				go = al.delta > 0;
			}
			if (go) {
				Integer top = pq.poll();
				while (pq.peek()!=null && pq.peek()==top)
					//remove duplicate markers
					pq.poll();
				if (top == null)
					break;
				n = top - ret.length();
				for (int i=0; i<n; i++) {
					byte[] votes = new byte['T'+1];
					for (Alignment a: rem) {
						if (a.position >= 0)
							votes[a.aligned.charAt(a.position)]++;
						a.position++;
						if (a.position == a.aligned.length())
							rem.remove(a);
					}
					int max = '-';
					for (int j='A'; j<='T'; i++)
						if (votes[max] < votes[j])
							max = j;
					ret.append(max);
				}
			}
			if (edge != null)
				pos += al.delta; //TODO inclusion?
		} while (true);
		return ret.toString(); //TODO remove gaps (not before!)
	}

	/**
	 * Combine alignments from two consecutive edges
	 * @param a alignment of the first sequence
	 * @param as first sequence
	 * @param b second sequence
	 * @param pos absolute position of the first sequence
	 * @return b's alignment with a
	 */
	public static Alignment getAlignment(String a, Sequence as, Sequence bs, int pos){
		StringBuilder r = new StringBuilder();
		AlignmentPath p= as.getAlignmentPath(bs);
		int i = a.length() - 1;
		int j;
		for (j = bs.length() - 1; j > p.start - 1; j--)
			r.append(bs.get(j));
		for (int k = p.pathlength - 1; k>=0 && i>=0; k--) {
			for (; a.charAt(i)=='-'; i--)
				r.append('-');
			switch (p.path[k]) {
				case AlignmentPath.LEFT_UP:
					i--;
					//fallthrough
				case AlignmentPath.LEFT:
					r.append(bs.get(j));
					j--;
					break;
				case AlignmentPath.UP:
					r.append('-');
			}
		}
		for (; j>=0; j--) {
			for (; a.charAt(i)=='-'; i--)
				r.append('-');
			r.append(bs.get(j));
		}
		return new Alignment(r.reverse().toString(), i, pos+i+r.length());
	}

	/**
	 * An edge of the graph used for the Hamiltonian path computation
	 * The value of from and to is an identifier of a Sequence.
	 * If there are N sequences, then N+i is the reverted complementary of the ith sequence. Indexed from 0.
	 */
	public final static class Edge implements Comparable<Edge>{
		public final int from;
		public final int to;
		public final short weight;
		public Edge(int f, int t, short w){
			from = f;
			to = t;
			weight = w;
		}
		@Override
		public int compareTo(Edge e){
			return this.weight - e.weight;
		}
		public String toString(){
			return "("+from+" --"+weight+"--> "+to+")";
		}
	}

	/**
	 * A thread to compute a part of all weights between edges
	 */
	private final static class EdgeComputer implements Runnable{
		private final List<Sequence> fragments;
		/**Means how much the work is distributed to thread. Also means the number of thread used for the entire work*/
		private final int divisor;
		/**What part of the work to compute*/
		private final int part;
		/**The list to add computed edges*/
		private final ArrayList<Edge> edges;

		public EdgeComputer(List<Sequence> in, ArrayList<Edge> out, int nThreads, int part){
			fragments = in;
			edges = out;
			divisor = nThreads;
			this.part = part;
		}

		@Override
		public void run(){
			computeEdges(divisor, part, fragments, edges);
		}
	}
}
