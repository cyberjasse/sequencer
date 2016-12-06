package sequencer;
/**
 * A data structure containing the score of an alignment and the path in the alignment matrix to retrace the alignement
 * @author Guillaume Huysmans, Jason Bury
 */
public class AlignmentPath{
	/** The score of the alignment*/
	public final int score;
	/**
	 * The position on the last line where the path starts in the alignement matrix.
	 * Yes, it starts always from the last line.
	 * It is also the position on the second sequence, -indexed from 1-, where the alignement finish.
	 */
	public final int start;
	/**
	 * The position where starts the alignment. Indexed from 1!
	 * If its positive, the alignement starts at the (position)th symbol from the first sequence
	 * But if its negative, its in case where the first sequence is included in the second, the alignement starts at the (-position)th symbol from the second sequence
	 */
	public final int delta;

	/**Symbols used to describe the path*/
	public static final byte LEFT=1, LEFT_UP=2, UP=3;
	/**The path. A list of symbols {LEFT, LEFT_UP, UP)*/
	public byte[] path;

	/**
	 * @param score The score of the semi-global alignment where the suffix of the first sequence is aligned with the prefix of the second.
	 * @param start The position on the last line where starts the alignment int the matrix.
	 * @param delta The position on the first sequence where starts the alignement.
	 * @param path The path.
	 */
	public AlignmentPath(int score, int start, int delta, byte[] path){
		this.score = score;
		this.start = start;
		this.delta = delta;
		this.path  = path;
	}

	public String toString(){
		String text="WEIGHT="+score+". start "+start+" then";
		for(int i=0; i<path.length ; i++){
			switch(path[i]){
				case LEFT : text+=" L,";break;
				case LEFT_UP : text+=" D,";break;
				case UP: text+=" U,";break;
			}
		}
		text+=" delta="+delta;
		return text;
	}

	public void compact(){
		byte[] newtab = new byte[path.length];
		for(int i=0 ; i<path.length ; i++){
			newtab[i] = path[i];
		}
		path = newtab;
	}
}
