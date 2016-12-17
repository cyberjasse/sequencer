package sequencer;

public class Alignment {
	public final String aligned;
	public final int delta;
	public int position = 0;

	public Alignment(String aligned, int delta) {
		this.aligned = aligned;
		this.delta = delta;
	}

	public boolean equals(Object other) {
		if (other instanceof Alignment) {
			Alignment o = (Alignment)other;
			return o.aligned.equals(aligned) && o.delta==delta;
		}
		else
			return false;
	}
}
