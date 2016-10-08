package sequencer;
import java.util.ArrayList;

/**
 * The main class. It contains the main method to get final consensus
 * @author Huysmans Guillaume, Buru Jason
 */
public class Sequencer{

	/**
	 * Enter the path of the fragments file as first parameter
	 */
	public static void main(String args[]){
		ArrayList<Sequency> fragments = load(args[0]);
		ArrayList<Sequency> consensus = getFinalConsensus(fragments);
		//TODO
	}

	/**
	 * Load a fasta file
	 * @param name The path of the file
	 * @return A list of loaded fragment. The inverted complementary of these fragment are not included in the list but you can get them with getComplementary() method in Sequency.
	 */
	public static ArrayList<Sequency> load(String name){
		//Perhaps a chained list ?
		ArrayList<Sequency> fragments = new ArrayList<Sequency>(150);//131 fragments in collection1.fasta
		//TODO guillaume
		return fragments;
	}

	/**
	 * Get final consensus
	 * @param fragments The list of fragments.
	 * @return All possible final consensus, including complementary. Hope there are not too many.
	 */
	public static ArrayList<Sequency> getFinalConsensus(ArrayList<Sequency> fragments){
		ArrayList<Sequency> consensus = new ArrayList<Sequency>();
		//TODO guillaume
		return consensus;
	}

	/**
	 * A node of the graph used for getFinalConsensus()
	 */
	private class Node{
		public final int from;
		public final int to;
		public final int weight;
		public Node(int f, int t, int w){
			from = f;
			to = t;
			weight = w;
		}
	}
}
		
