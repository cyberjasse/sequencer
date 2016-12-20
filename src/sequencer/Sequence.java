package sequencer;
/**
 * A DNA fragment
 * @author Guillaume Huysmans, Jason Bury
 */

public class Sequence{
	protected String fragment;
	protected Sequence complementary;
	//a short is between -32768 and 32767. And if the maximum length of a fragment is 800, score is between -1600 and 800
	private static final short GAP_SCORE = -2;//-g
	private static final short MATCH_SCORE = 1;
	private static final short MISMATCH_SCORE = -1;

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
	 * Perform the semi global alignment to 2 sequences.
	 * @param other The other sequence
	 * @return The matrix needed to compute the score or the resulting consensus.
	 */
	public short[][] semiGlobalAlignment(Sequence other){
		short m = (short)this.fragment.length();
		short n = (short)other.fragment.length();
		short[][] a = new short[m+1][n+1];
		//Values in matrice at first
		short i, j;
		for(i=0 ; i<=m ; i++)
			a[i][0] = 0;
		for(j=0 ; j<=n ; j++)
			a[0][j] = 0;
		//Applied the recurrence with an iterative implementation
		short score;
		for(i=1 ; i<=m ; i++){
			for(j=1 ; j<=n ; j++){
				short im1 = (short)(i-1), jm1 = (short)(j-1);//to not compute them 3 times
				if(this.get(im1) == other.get(jm1))
					score = MATCH_SCORE;
				else
					score = MISMATCH_SCORE;
				a[i][j] = (short)Math.max(a[im1][j]+GAP_SCORE,
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
	private byte nextPosition(short i, short j, short[][] matrix, Sequence other){
		short matchscore;
		byte where;
		short im1 = (short)(i-1);
		short jm1 = (short)(j-1);
		if(get(im1)==other.get(jm1))
			matchscore = MATCH_SCORE;
		else
			matchscore = MISMATCH_SCORE;
		short fromscore = (short)(matrix[im1][j]+GAP_SCORE);
		where = AlignmentPath.UP;
		short otherscore = (short)(matrix[im1][jm1]+matchscore);
		if( otherscore > fromscore){
			fromscore = otherscore;
			where = AlignmentPath.LEFT_UP;
		}
		otherscore = (short)(matrix[i][jm1]+GAP_SCORE);
		if( otherscore > fromscore){
			return AlignmentPath.LEFT;
		}
		return where;
	}

	/**
	 * Compute the path in a matrix (start on the last line)
	 * @param start The position of the start on the last line
	 */
	private AlignmentPath pathMatrix(short[][] matrix, short start, Sequence other){
		short j = start;
		short i = (short)(matrix.length-1);//The number of lines -1
		short score = matrix[i][j];
		//The worst-case size of the path
		//In a worst case, the path pass along all the last column than pass along all a line
		short maxsize = (short)(matrix.length + matrix[0].length);
		maxsize--;
		byte[] pathfg = new byte[maxsize];
		short pathsize = 0;
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
		short delta;
		if(j>1)//so i=1, we are on the "first" line
			delta = (short)-j;
		else
			delta = i;
		//build the AlignmentPath for score(f g)
		return new AlignmentPath(score,start,delta,pathfg,pathsize);
	}

	/**
	 * Compute the path in the transpoded of matrix (start on the last line).
	 * It will not really transpose the matrix.
	 * @param start The position of the start on the last column of matrix
	 */
	private AlignmentPath pathRevertedMatrix(short[][] matrix, short start, Sequence other){
		//build the path but with a transpose matrix.
		//there's no need to really transpose it, we just swap LEFT and UP.
		short maxsize = (short)Math.max(matrix.length , matrix[0].length);
		byte[] pathgf = new byte[maxsize];
		short i = start;
		short j = (short)(matrix[0].length-1);//number of columns-1
		short score = matrix[i][j];
		short pathsize = 0;
		byte pathsymbol;
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
		short delta;
		if(i>1)//so j=1, we are on the "first" line of the transpose matrix
			delta = (short)-i;
		else
			delta = j;
		//build the AlignmentPath for score(g f)
		return new AlignmentPath(score,start,delta,pathgf,pathsize);
	}

	/**
	 * Compute score of a semi global alignment.
	 * Let f and g two sequences.
	 * Let f' and g' the reverted complementary of f and g respectively.
	 * Let score(f g) The score of the semi-global alignment forcing the suffix of f to be aligned with the prefix of g OR f included in g.
	 * So
	 * The first score returned is score(f g)=score(g' f').
	 * The second returned is score(g f)=score(f' g').
	 * @param other g, The second sequence
	 * @return return score(f g) and score(g' f')
	 */
	public short[] getAlignmentScore(Sequence other){
		short[] scores = new short[2];
		short[][] matrix = semiGlobalAlignment(other);
		//score(f g)
		scores[0] = matrix[fragment.length()][1];//length is a field in the String object
		short i, j;
		//search the score
		for(j=(short)2 ; j<=other.fragment.length() ; j++){
			if( matrix[fragment.length()][j] > scores[0] ){
				scores[0] = matrix[fragment.length()][j];
			}
		}
		//score(g f)
		scores[1] = matrix[1][other.fragment.length()];
		//search the score
		for(i=2 ; i<fragment.length() ; i++){
			if(matrix[i][other.fragment.length()] > scores[1]){
				scores[1] = matrix[i][other.fragment.length()];
			}
		}
		return scores;
	}

	/**
	 * Compute the AlignmentPath of score(f g) The score of the semi-global alignment forcing the suffix of f to be aligned with the prefix of g OR f included in g.
	 * note: it will compute the alignment matrix.
	 */
	public AlignmentPath getAlignmentPath(Sequence other){
		short[][] matrix = semiGlobalAlignment(other);
		short score = matrix[fragment.length()][1];//length is a field in the String object
		short j;
		short start = (short)1;
		//search the position of the score
		for(j=(short)2 ; j<=other.fragment.length() ; j++){
			if( matrix[fragment.length()][j] > score ){
				score = matrix[fragment.length()][j];
				start = j;
			}
		}
		return pathMatrix(matrix, start, other);
	}

	/**
	 * Compute the AlignmentPath of the reverted matrix of score(f g) The score of the semi-global alignment forcing the suffix of f to be aligned with the prefix of g OR f included in g.
	 * note: it will compute the alignment matrix.
	 */
	public AlignmentPath getRevertedAlignmentPath(Sequence other){
		short[][] matrix = semiGlobalAlignment(other);
		short score = matrix[1][other.fragment.length()];//length is a field in the String object
		short i;
		short start = (short)1;
		//search the position of the score
		for(i=(short)2 ; i<=fragment.length() ; i++){
			if( matrix[i][other.fragment.length()] > score){
				score = matrix[i][other.fragment.length()];
				start = i;
			}
		}
		return pathRevertedMatrix(matrix, start, other);
	}

	/**
	 * Compute The score in case of an inclusion of sequences
	 */
	public short getInclusionScore(Sequence other){
		return (short)-1;//TODO ?
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

	public int length() {
		return fragment.length();
	}
}
