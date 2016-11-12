package sequencer;
import java.util.ArrayList;
import java.util.List;

/**
 * The main class computing the final consensus
 * @author Guillaume Huysmans, Jason Bury
 */
public class Sequencer{
	/**
	 * Enter the path of the fragments file as first parameter
	 */
	public static void main(String args[]){
		Sequence s1 = new Sequence("at");
		Sequence s2 = new Sequence("tc");
		Sequence s3 = new Sequence("cg");
		ArrayList<Sequence> l = new ArrayList<Sequence>(3);
		l.add(s1);
		l.add(s2);
		l.add(s3);
		Sequencer s = new Sequencer();
		s.allEdges(l);
	}

	/**
	 * Compute all AlignmentPath
	 * @param fragments The list of Sequence, like returned by the load() method
	 * @return A list of all edges between all pairs of Sequence in fragments and their reverted complementary.
	 * If the length of fragments is N, then N+i is the reverted complementary of the ith fragment. Indexed from 0.
	 */
	public static List<Edge> allEdges(List<Sequence> fragments){//TODO Paralelize
		int i,j;
		int N = fragments.size();
		ArrayList<Edge> edges = new ArrayList<Edge>(4*((N*N) - N));
		//build all edges
		for(i=0; i<N ; i++){
			for(j=i+1 ; j<N ; j++){
				//i is f, j is g. i+N is f', j+N is g'
				AlignmentPath[] aps = fragments.get(i).getAlignmentScore(fragments.get(j));
				edges.add(new Edge( i, j, aps[0] ));//add {f,g}
				edges.add(new Edge( j, i, aps[1] ));//add {g,f}
				edges.add(new Edge( j+N, i+N, aps[0] ));//add {g',f'}
				edges.add(new Edge( i+N, j+N, aps[1] ));//add {f',g'}
				AlignmentPath[] aps2 = fragments.get(i).getAlignmentScore(fragments.get(j).getComplementary());
				edges.add(new Edge( i, j+N, aps[0] ));//add {f,g'}
				edges.add(new Edge( j+N, i, aps[1] ));//add {g',f}
				edges.add(new Edge( j, i+N, aps[0] ));//add {g,f'}
				edges.add(new Edge( i+N, j, aps[1] ));//add {f',g}
			}
		}
		return edges;
	}

	/**
	 * Compute the hamiltonian path
	 * @param edges The list of all edges
	 * @return The list of ordered edges, the entire hamiltonian path.
	 */
	public static List<Edge> hamiltonian(List<Edge> edges){
		//TODO jason
		List<Edge> hamiltonian = null;
		return hamiltonian;
	}

	/**
	 * Load a fasta file. The inverted complementary of these fragments aren't
	 * included but you can get them using #Sequence.getComplementary().
	 * @param name The path of the file
	 * @return A list of loaded fragments.
	 */
	public static List<Sequence> load(String name){
		//Perhaps a linked list ?
		List<Sequence> fragments = new ArrayList<Sequence>(150);
		//131 fragments in collection1.fasta
		//TODO guillaume
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
	private final static class Edge{
		public final int from;
		public final int to;
		public final AlignmentPath weight;
		public Edge(int f, int t, AlignmentPath w){
			from = f;
			to = t;
			weight = w;
		}
	}
}
