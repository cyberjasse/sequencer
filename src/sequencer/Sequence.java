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
	 * Compute The score of a semi global alignment.
	 * @param other The other sequence
	 * @return The score.
	 */
	public int getAlignementScore(Sequence other){
		return -1;//TODO Jason
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
}
