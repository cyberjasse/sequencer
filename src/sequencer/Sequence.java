package sequencer;
/**
 * A DNA fragment
 * @author Guillaume Huysmans, Jason Bury
 */

public class Sequence{
	protected String fragment;
	protected Sequence complementary;

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
	private int[][] semiGlobalAlignment(Sequence other){
		return new int[0][0];//TODO Jason perhaps return a char[][] ?
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
