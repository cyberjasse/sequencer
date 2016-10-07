package sequencer;
/**
 * A DNA fragment.
 * @author Huysmans Guillaume, Bury Jason
 */

public class Sequency{
	protected String fragment;
	protected Sequency complementary;

	/**
	 * @param charset The DNA fragment.
	 */
	public Sequency(String charset){
		fragment = charset;
		complementary = new Complementary(charset, this);
	}

	/**
	 * Perform the semi global alignment to 2 sequencies.
	 * @param other The second sequency to perform the semi-global alignment.
	 * @return The matrix needed to compute the score or the resulting consensus.
	 */
	private int[][] semiGlobalAlignment(Sequency other){
		return new int[0][0];//TODO Jason perhaps return a char[][] ?
	}

	/**
	 * @param i The index of the character. Indexed from 0.
	 * @return The i-1 th character of the sequency.
	 */
	public char get(int i){
		return fragment.charAt(i);
	}

	/**
	 * Compute The score of a semi global alignment.
	 * @param other The second sequency to perform the semi-global alignement.
	 * @return The score.
	 */
	public int getAlignementScore(Sequency other){
		return -1;//TODO Jason
	}

	/**
	 * Compute The score in case of an inclusion of sequencies
	 */
	public int getInclusionScore(Sequency other){
		return -1;//TODO ?
	}

	/**
	 * Get the consensus resulting of a semi-global alignment to 2 sequencies.
	 * @param other The second sequency.
	 */
	public Sequency getConsensus(Sequency other){
		return null;//TODO ?
	}

	/**
	 * Get the inverted complementary.
	 */
	public Sequency getComplementary(){
		return complementary;
	}
}
