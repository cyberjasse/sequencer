package sequencer;
/**
 * A data structure containing the score of an alignment and the path in the alignment matrix to retrace the alignment
 * @author Guillaume Huysmans, Jason Bury
 */
public class AlignmentPath{
	/** The score of the alignment*/
	public final short score;
	/**
	 * The position on the last line where the path starts in the alignment
	 * matrix (it always starts from the last line).
	 * It is also the position on the second sequence - indexed from 1 - where
	 * the alignment finishes.
	 */
	public final short start;
	/**
	 * The position where starts the alignment. Indexed from 1!
	 * If its positive, the alignment starts at the (position)th symbol from the first sequence
	 * But if its negative, its in case where the first sequence is included in the second, the alignment starts at the (-position)th symbol from the second sequence
	 */
	public final short delta;

	/**Symbols used to describe the path*/
	public static final byte LEFT=1, LEFT_UP=2, UP=3;
	/**The path. A list of symbols {LEFT, LEFT_UP, UP)*/
	public byte[] path;
	/**The number of symbols in path*/
	public final short pathlength;

	/**
	 * @param score The score of the semi-global alignment where the suffix of the first sequence is aligned with the prefix of the second.
	 * @param start The position on the last line where starts the alignment int the matrix.
	 * @param delta The position on the first sequence where starts the alignment.
	 * @param path The path.
	 * @param pathSize The length of the path.
	 */
	public AlignmentPath(short score, short start, short delta, byte[] path, short pathLength){
		this.score = score;
		this.start = start;
		this.delta = delta;
		this.path  = path;
		this.pathlength = pathLength;
	}

	public String toString(){
		String text="WEIGHT="+score+". start "+start+" then";
		for(short i=0; i<pathlength ; i++){
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
		byte[] newtab = new byte[pathlength];
		for(short i=0 ; i<pathlength ; i++){
			newtab[i] = path[i];
		}
		path = newtab;
	}
}
