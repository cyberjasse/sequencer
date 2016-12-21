package sequencer;
/**
 * The inverted complementary of a DNA sequence
 * @author Guillaume Huysmans, Jason Bury
 */

public class Complementary extends Sequence {
	/**
	 * @param initial initial fragment, not the complementary
	 * @param first the "normal" fragment
	 */
	public Complementary(String initial, Sequence first){
		super(initial, first);
	}

	/**
	 * Read a nucleotide from the end of the original sequence, complementing it.
	 */
	@Override
	public char get(int i){
		char c = fragment.charAt(fragment.length()-i-1);
		switch(c){
			case 'a': return 't';
			case 't': return 'a';
			case 'c': return 'g';
			default : return 'c';
		}
	}

	@Override
	public String toString() {
		return null; //crashing is better than returning something incorrect
	}
}
