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

	/**
	 * Create {{i}|0<=i<Nnodes}
	 * @param Nnodes The number of nodes.
	 */
	public UnionFind(int Nnodes){
		N = Nnodes;
		parent = new int[N];
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
		parent[px] = py;
	}
}
