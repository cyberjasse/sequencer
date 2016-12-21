package sequencer;
import java.util.ArrayList;
import java.util.List;
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
	 * Compute a part of all edges and their weight
	 * @param divisor How much the work is divided. It computes 1/divisor of all paths
	 * @param part What part of the work to compute. Indexed from 0
	 * @param fragments
	 * @param edges The list to add edges
	 */
	private static void computeEdges(int divisor, int part, List<Sequence> fragments, List<Edge> edges){
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
	public static List<Edge> allEdges(List<Sequence> fragments, int nThreads){
		List<Edge> edges = new ArrayList<Edge>(4*((fragments.size()*fragments.size()) - fragments.size()));
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
		List<Edge> hamiltonian = new ArrayList<Edge>( nSequences-1);
		while(out[i]){//while we don't reach the end of the path
			hamiltonian.add(nextEdges[i]);
			i = nextEdges[i].to;
		}
		return hamiltonian;
	}

	/**
	 * Get a sequence (or its reverted complementary) from a list of Sequences
	 * @param id index
	 */
	private static Sequence getById(List<Sequence> list, int id){
		int n = list.size();
		if (id >= n)
			return list.get(id - n).getComplementary();
		else
			return list.get(id);
	}

	/**
	 * Compute a consensus
	 * @param fragments The list of fragments
	 * @param path A Hamiltonian path
	 * @return A possible final consensus
	 */
	public static Sequence getConsensus(List<Sequence> frags, List<Edge> path){
		//populate the marker queue
		Iterator<Edge> it = path.iterator();
		PriorityQueue<Marker> pq = new PriorityQueue<>();
		//let's not forget the first sequence
		Alignment al = new Alignment(getById(frags, path.get(0).from).toString(), 0, 0, 0);
		pq.add(new Marker(0, al));
		int pos = 0;
		while (it.hasNext()) {
			Edge e = it.next();
			Sequence a = getById(frags, e.from);
			Sequence b = getById(frags, e.to);
			al = getAlignment(al.aligned, a, b, pos);
			pos += al.delta;
			pq.add(new Marker(pos, al));
		}

		//build the consensus
		StringBuilder ret = new StringBuilder();
		List<Alignment> started = new ArrayList<>();
		pos = pq.peek().pos;
		while (pq.size() > 0) {
			Marker top = pq.poll();
			while (pq.peek()!=null && pq.peek().equals(top))
				//remove duplicate markers
				pq.poll();
			top.alignment.position = 0;
			started.add(top.alignment);
			while ((pq.peek()==null && started.size()>0) || (pq.peek()!=null && pq.peek().pos>pos)) {
				byte[] votes = new byte['t'+1];
				for (Iterator<Alignment> at = started.iterator(); at.hasNext();) {
					Alignment a = at.next();
					if (a.position == a.aligned.length())
						at.remove();
					else {
						votes[a.aligned.charAt(a.position)]++;
						a.position++;
					}
				}
				char max = '-';
				//TODO unfold this loop? What about ties?
				for (char j='a'; j<='t'; j++)
					if (votes[max] < votes[j])
						max = j;
				if (max != '-')
					//don't output gaps
					ret.append(max);
				pos++;
			}
		}
		return new Sequence(ret.toString());
	}

	private final static class Marker implements Comparable<Marker> {
		public final Alignment alignment;
		public final int pos;

		public Marker(int pos, Alignment alignment) {
			this.alignment = alignment;
			this.pos = pos;
		}

		@Override
		public int compareTo(Marker other) {
			return pos - other.pos;
		}
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
		String ret = r.reverse().toString();
		int endsAt = pos+i+r.length()-1;
		if (p.delta < 0) //inclusion: i==0
			return new Alignment(ret, p.delta+1, endsAt-1, pos+p.delta+1);
		else
			return new Alignment(ret, i, endsAt, -i);
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
		private final List<Edge> edges;

		public EdgeComputer(List<Sequence> in, List<Edge> out, int nThreads, int part){
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
