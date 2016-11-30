package sequencer;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Comparable;
import java.util.Collections;
import java.util.Comparator;
import java.lang.Runnable;

/**
 * The main class computing the final consensus
 * @author Guillaume Huysmans, Jason Bury
 */
public class Sequencer{
	/**
	 * Enter the path of the fragments file as first parameter
	 */
	public static void main(String args[]){
		try {
			List<Sequence> fragments = load(args[1]);
			List<Edge> edges = allEdges(fragments, 1);
			List<Edge> path = hamiltonian(edges, fragments.size());
			System.out.println(fragments.size()+" fragments");
			System.out.println(edges.get(0).from);
			for (Edge e: path)
				System.out.println(e.to);
		}
		catch (Exception e) {
			e.printStackTrace();
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
		System.out.println("(computerEdges) thread "+part+" START");
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
		System.out.println("(computerEdges) thread "+part+" END");
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
	 * Get final consensus (hope there are not too many...)
	 * @param fragments The list of fragments.
	 * @return All possible final consensuses, including complementary fragments.
	 */
	public static List<Sequence> getFinalConsensus(List<Sequence> fragments){
		List<Sequence> consensus = new ArrayList<Sequence>();
		//TODO guillaume
		return consensus;
	}

	/**
	 * A edge of the graph used for the hamiltonian path research
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
