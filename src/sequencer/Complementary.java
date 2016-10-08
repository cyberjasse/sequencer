package sequencer;
/**
 * The inverted complementary of a DNA sequencies.
 * (Difference is in the get() method)
 * @author Huysmans Guillaume, Bury Jason
 */

public class Complementary extends Sequency{
	private final int size;

	/**
	 * @param charset The initial fragment. Not the complementary.
	 * @param first A reference to the Sequency object of the initial fragment.
	 */
	public Complementary(String charset, Sequency first){
		fragment = charset;
		complementary = first;
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
