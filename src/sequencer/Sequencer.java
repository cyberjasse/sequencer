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
	 */
	public List<Edge> allEdges(List<Sequence> fragments){//TODO Paralelize
		int i,j;
		//build nodes
		Node[] nodes = new Node[fragments.size()];
		for(i=0 ; i<fragments.size() ; i++){
			Node n = new Node(fragments.get(i));
			nodes[i] = n;
		}
		//TODO check if the size of deges defined below is correct
		ArrayList<Edge> edges = new ArrayList<Edge>(2*(fragments.size()*fragments.size() - fragments.size()));//Because Let N the fragments length, they are N*N-N pairs of {f,g}or{g,f} f!=g. Then 2X because we add pairs {f' g'}or{g' f'}
		//build all edges
		for(i=0; i<fragments.size() ; i++){
			for(j=i+1 ; j<fragments.size() ; j++){
				//nodes[i] is f, nodes[j] is g
				AlignmentPath[] aps = nodes[i].sequence.getAlignmentScore(nodes[j].sequence);
				edges.add(new Edge( nodes[i],nodes[j],aps[0] ));//add {f,g}
				edges.add(new Edge( nodes[j],nodes[i],aps[1] ));//add {g,f}
				edges.add(new Edge( nodes[j].complementary,nodes[i].complementary,aps[0] ));//add {g',f'}
				edges.add(new Edge( nodes[i].complementary,nodes[j].complementary,aps[1] ));//add {f',g'}
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
	 * A Node of the graph used for the hamiltonian path research
	 */
	private class Node{
		public boolean in;
		public boolean out;
		public final Sequence sequence;
		/**The node containing the reverted complementary of sequence*/
		public final Node complementary;
		private Node(Sequence s, Node comp){
			in = false;
			out = false;
			sequence = s;
			complementary = comp;
		}
		public Node(Sequence s){
			in = false;
			out = false;
			sequence = s;
			complementary = new Node(s.getComplementary(), this);
		}
	}

	/**
	 * A edge of the graph used for the hamiltonian path research
	 */
	private class Edge{
		public final Node from;
		public final Node to;
		public final AlignmentPath weight;
		public Edge(Node f, Node t, AlignmentPath w){
			from = f;
			to = t;
			weight = w;
		}
	}
}
