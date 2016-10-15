package sequencer;
/**
 * The inverted complementary of a DNA sequence
 * @author Huysmans Guillaume, Bury Jason
 */

public class Complementary extends Sequence {
	private final int size;

	/**
	 * @param initial initial fragment, not the complementary
	 * @param first the "normal" fragment
	 */
	public Complementary(String initial, Sequence first){
		super(initial, first);
		size = fragment.length()-1;
	}

	@Override
	public char get(int i){
		char c = fragment.charAt(size-i);
		switch(c){
			case 'a': return 't';
			case 't': return 'a';
			case 'c': return 'g';
			default : return 'c';
		}
	}
}
