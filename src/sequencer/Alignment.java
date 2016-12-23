package sequencer;

public class Alignment {
	public final String aligned;
	public final int delta;
	public int position = 0;

	public Alignment(String aligned, int delta) {
		this.aligned = aligned;
		this.delta = delta;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Alignment) {
			Alignment o = (Alignment)other;
			return o.aligned.equals(aligned) && o.delta==delta;
		}
		else
			return false;
	}

	@Override
	public int hashCode() {
		return aligned.hashCode();
	}

	@Override
	public String toString() {
		return aligned+", d "+delta;
	}
}
