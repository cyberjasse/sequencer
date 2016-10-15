package sequencer;
import java.util.ArrayList;
import java.util.List;

/**
 * The main class. It contains the main method to get final consensus
 * @author Huysmans Guillaume, Buru Jason
 */
public class Sequencer{

	/**
	 * Enter the path of the fragments file as first parameter
	 */
	public static void main(String args[]){
		List<Sequence> fragments = load(args[0]);
		List<Sequence> consensus = getFinalConsensus(fragments);
		//TODO
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
	 * Get final consensus
	 * @param fragments The list of fragments.
	 * @return All possible final consensus, including complementary. Hope there are not too many.
	 */
	public static List<Sequence> getFinalConsensus(List<Sequence> fragments){
		List<Sequence> consensus = new ArrayList<Sequence>();
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
		
