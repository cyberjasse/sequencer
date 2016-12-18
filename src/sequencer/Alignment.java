package sequencer;

public class Alignment {
	public final String aligned;
	public final int delta;
	public int position;
	public final int endsAt;

	public Alignment(String aligned, int delta, int endsAt) {
		this.aligned = aligned;
		this.delta = delta;
		this.position = -delta;
		this.endsAt = endsAt;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Alignment) {
			Alignment o = (Alignment)other;
			//TODO compare endsAt too (and update the unit test)!
			return o.aligned.equals(aligned) && o.delta==delta;
		}
		else
			return false;
	}

	@Override
	public int hashCode() {
		return aligned.hashCode() ^ position;
	}
}
