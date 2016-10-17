package sequencer;
/**
 * A DNA fragment
 * @author Guillaume Huysmans, Jason Bury
 */

public class Sequence{
	protected String fragment;
	protected Sequence complementary;
	private static final int FIRST_GAP_SCORE = -4;//-h TODO choose a good value for -h and -g. See page 67 https://moodle.umons.ac.be/mod/resource/view.php?id=2559
	private static final int GAP_SCORE = -2;//-g
	private static final int MATCH_SCORE = 1;
	private static final int MISMATCH_SCORE = -1;
	private static final int NEGATIVE_INFINITY = Integer.MIN_VALUE;// int in java does not support a real infinity value. Double does but can take too much memory

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
	 * The max method for 3 parameters
	 */
	private int max(int a, int b, int c){
		if(a > b){
			if(a > c) return a;
			else return c;
		}
		else{
			if(b > c) return b;
			else return c;
		}
	}

	/**
	 * Perform the semi global alignment to 2 sequences.
	 * @param other The other sequence
	 * @return The matrix needed to compute the score or the resulting consensus.
	 */
	private int[][] semiGlobalAlignment(Sequence other){
		int m = this.fragment.length();
		int n = other.fragment.length();
		int[][] a = new int[m][n];//Score of best alignment ending by the pair Si Tj
		int[][] b = new int[m][n];//Score of best alignment ending by the pair _  Tj
		int[][] c = new int[m][n];//Score of best alignment ending by the pair Si  _
		//Values in matrices at first
		a[0][0] = 0;
		int i, j;
		for(i=0 ; i<m ; i++){
			a[i][0] = NEGATIVE_INFINITY;
			b[i][0] = NEGATIVE_INFINITY;
			c[i][0] = FIRST_GAP_SCORE + GAP_SCORE*i;
		}
		for(j=0 ; j<n ; j++){
			a[0][j] = NEGATIVE_INFINITY;
			b[0][j] = FIRST_GAP_SCORE + GAP_SCORE*j;
			c[0][j] = NEGATIVE_INFINITY;
		}
		//Applied the reccurence with an iterative implementation
		for(i=1 ; i<m ; i++){
			for(j=1 ; j<n ; j++){
				int score;
				if(get(i) == other.get(j)){
					score = MATCH_SCORE;
				}
				else{
					score = MISMATCH_SCORE;
				}
				int im1 = i-1, jm1 = j-1;//to not compute them 6 times
				int hpg = (FIRST_GAP_SCORE+GAP_SCORE);//to not compute it 4 times.
				a[i][j] = score + max(a[im1][jm1], b[im1][jm1], c[im1][jm1]);//FIXME: can it be more than maximum int value ?
				b[i][j] = max(hpg+a[i][jm1], GAP_SCORE+b[i][jm1], hpg+c[i][jm1]);
				c[i][j] = max(hpg+a[im1][j], hpg+b[im1][j], GAP_SCORE+c[im1][j]);
			}
		}
		//Then return matrice containing the score of the best alignment
		int mm1 = m-1, nm1 = n-1;
		if(a[mm1][nm1] > b[mm1][nm1]){
			if(a[mm1][nm1] > c[mm1][nm1]){
				return a;
			}
			else{
				return c;
			}
		}
		else{
			if(b[mm1][nm1] > c[mm1][nm1]){
				return b;
			}
			else{
				return c;
			}
		}
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
