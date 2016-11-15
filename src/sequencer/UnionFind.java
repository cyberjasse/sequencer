package sequencer;
/**
 * Our implementation of the simple union-find data structure. Since there is no Union-find in the java7 common library
 * @author Guillaume Huysmans, Jason Bury
 */
public class UnionFind{
	/** parent[i] is the parent of the node i*/
	private int[] parent;
	/** The number of nodes*/
	private final int N;
	/** The number of sets*/
	private int nSets;

	/**
	 * Create {{i}|0<=i<Nnodes}
	 * @param Nnodes The number of nodes.
	 */
	public UnionFind(int Nnodes){
		N = Nnodes;
		parent = new int[N];
		nSets = N;
		for(int i=0 ; i<N ; i++){
			parent[i] = i;
		}
	}

	/**
	 * @return The root of the tree (the set) containing x
	 */
	public int find(int x){
		if(x == parent[x]){
			return x;
		}
		return find(parent[x]);
	}

	/**
	 * Union the set containing x to the set containing y
	 */
	public void union(int x, int y){
		int px = find(x);
		int py = find(y);
		directUnion(px, py);
	}

	/**
	 * Union two sets providing their root
	 * @param rootx The root of a set
	 * @param rooty The root of another set
	 */
	public void directUnion(int rootx, int rooty){
		parent[rootx] = rooty;
		nSets--;
	}

	/**
	 * Ignore the node x. It sets is parent to -1 and decrement the number of sets.
	 * The user have to ensure that the set containing x has a cardinality of 1. And it will not be used anymore.
	 */
	public void ignore(int x){
		if(parent[x] != -1){
			parent[x] = -1;
			nSets--;
		}
	}

	/**
	 * @return The number of sets
	 */
	public int getNsets(){
		return nSets;
	}
}
