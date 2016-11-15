package sequencer;
/**
 * A DNA fragment
 * @author Guillaume Huysmans, Jason Bury
 */

public class Sequence{
	protected String fragment;
	protected Sequence complementary;
	private static final int GAP_SCORE = -2;//-g
	private static final int MATCH_SCORE = 1;
	private static final int MISMATCH_SCORE = -1;
	private static final int NEGATIVE_INFINITY = -1000000000;//FIXME int in java does not support a real infinity value. Double does but can take too much memory

	/**
	 * @param seq The DNA fragment
	 */
	public Sequence(String seq){
		fragment = seq;
		complementary = new Complementary(seq, this);
	}

	protected Sequence(String seq, Sequence comp){
		fragment = seq;
		complementary = comp;
	}

	/**
	 * Print a int[][] table for debugging
	 * @param tab The table
	 * @param nrows The number of rows
	 * @param ncols The number of column
	 */
	private void printtab(int[][] tab, int nrows, int ncols){
		for(int i=0 ; i<nrows ; i++){
			for(int j=0 ; j<ncols ; j++){
				int num = tab[i][j];
				if(num <= NEGATIVE_INFINITY){
					System.out.print(" -IY");
				}
				else{
					String st = Integer.toString(num);
					if(st.length() > 3) System.out.print(st = " BIG");
					else System.out.print(String.format(" %1$03d",num));
				}
			}
			System.out.print("\n");
		}
	}

	/**
	 * Perform the semi global alignment to 2 sequences.
	 * @param other The other sequence
	 * @return The matrix needed to compute the score or the resulting consensus.
	 */
	public int[][] semiGlobalAlignment(Sequence other){
		int m = this.fragment.length();
		int n = other.fragment.length();
		int[][] a = new int[m+1][n+1];
		//Values in matrice at first
		int i, j;
		for(i=0 ; i<=m ; i++)
			a[i][0] = 0;
		for(j=0 ; j<=n ; j++)
			a[0][j] = 0;
		//Applied the recurrence with an iterative implementation
		int score;
		for(i=1 ; i<=m ; i++){
			for(j=1 ; j<=n ; j++){
				int im1 = i-1, jm1 = j-1;//to not compute them 3 times
				if(this.get(im1) == other.get(jm1))
					score = MATCH_SCORE;
				else
					score = MISMATCH_SCORE;
				a[i][j] = Math.max(a[im1][j]+GAP_SCORE,
					Math.max(a[im1][jm1]+score,
					a[i][jm1]+GAP_SCORE));
			}
		}
		return a;
	}

	/**
	 * @param i The zero-based index of the character
	 * @return The i-1 th character of the sequence.
	 */
	public char get(int i){
		return fragment.charAt(i);
	}

	/**
	 * From the position [i][j] on a alignement matrix (for this and other), return an AlignmentPath path symbol to indicate from where the score on [i][j] comes from
	 */
	private byte nextPosition(int i, int j, int[][] matrix, Sequence other){
		int matchscore;
		byte where;
		if(get(i-1)==other.get(j-1))
			matchscore = MATCH_SCORE;
		else
			matchscore = MISMATCH_SCORE;
		int fromscore = matrix[i-1][j]+GAP_SCORE;
		where = AlignmentPath.UP;
		int otherscore = matrix[i-1][j-1]+matchscore;
		if( otherscore > fromscore){
			fromscore = otherscore;
			where = AlignmentPath.LEFT_UP;
		}
		otherscore = matrix[i][j-1]+GAP_SCORE;
		if( otherscore > fromscore){
			return AlignmentPath.LEFT;
		}
		return where;
	}

	/**
	 * Compute score of a semi global alignment.
	 * Let f and g two sequences.
	 * Let f' and g' the reverted complementary of f and g respectively.
	 * Let score(f g) The score of the semi-global alignment forcing the suffix of f to be aligned with the prefix of g OR f included in g.
	 * So
	 * The first AlignmentPath returned contains score(f g)=score(g' f') and path in the alignment matrix.
	 * The second AlignmentPath returned contains score(g f)=score(f' g') and path.
	 * @param other g, The second sequence
	 * @return return two scores with their path in the alignment matrix
	 */
	public AlignmentPath[] getAlignmentScore(Sequence other){
		int[][] matrix = semiGlobalAlignment(other);
		//score(f g)
		int score = matrix[fragment.length()][1];//length is a field in the String object
		int start = 1;
		int i, j;
		//search the score and start position
		for(j=2 ; j<=other.fragment.length() ; j++){
			if( matrix[fragment.length()][j] > score ){
				score = matrix[fragment.length()][j];
				start = j;
			}
		}
		//build the path
		j = start;
		i = fragment.length();
		int maxsize = Math.max(fragment.length() , other.fragment.length());
		byte[] pathfg = new byte[maxsize];
		int pathsize = 0;
		byte pathsymbol;
		while(i>1 && j>1){
			pathsymbol = nextPosition(i,j,matrix,other);
			pathfg[pathsize] = pathsymbol;
			if(pathsymbol == AlignmentPath.UP)
				i--;
			else if(pathsymbol == AlignmentPath.LEFT)
				j--;
			else{
				i--;
				j--;
			}
			pathsize++;
		}
		int delta;
		if(j>1)//so i=1, we are on the "first" line
			delta = -j;
		else
			delta = i;
		//build the AlignmentPath for score(f g)
		AlignmentPath[] paths = new AlignmentPath[2];
		paths[0] = new AlignmentPath(score,start,delta,pathfg,pathsize);
		//score(g f)
		score = matrix[1][other.fragment.length()];
		start = 1;
		//search the start position
		for(i=2 ; i<fragment.length() ; i++){
			if(matrix[i][other.fragment.length()] > score){
				score = matrix[i][other.fragment.length()];
				start = i;
			}
		}
		//build the path but with a transpose matrix.
		//there's no need to really transpose it, we just swap LEFT and UP.
		byte[] pathgf = new byte[maxsize];
		i = start;
		j = other.fragment.length();
		pathsize = 0;
		while(i>1 && j>1){
			pathsymbol = nextPosition(i,j,matrix,other);
			if(pathsymbol == AlignmentPath.UP){
				pathgf[pathsize] = AlignmentPath.LEFT;
				i--;
			}
			else if(pathsymbol == AlignmentPath.LEFT){
				pathgf[pathsize] = AlignmentPath.UP;
				j--;
			}
			else{
				pathgf[pathsize] = pathsymbol;
				i--;
				j--;
			}
			pathsize++;
		}
		if(i>1)//so j=1, we are on the "first" line of the transpose matrix
			delta = -i;
		else
			delta = j;
		//build the AlignmentPath for score(g f)
		paths[1] = new AlignmentPath(score,start,delta,pathgf,pathsize);
		return paths;
	}

	/**
	 * Compute The score in case of an inclusion of sequences
	 */
	public int getInclusionScore(Sequence other){
		return -1;//TODO ?
	}

	/**
	 * Get the consensus resulting of a semi-global alignment to 2 sequences.
	 * @param other The other sequence
	 */
	public Sequence getConsensus(Sequence other){
		return null;//TODO ?
	}

	/**
	 * Get the inverted complementary.
	 */
	public Sequence getComplementary(){
		return complementary;
	}

	@Override
	public String toString() {
		return fragment;
	}
}
