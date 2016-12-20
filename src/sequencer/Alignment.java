package sequencer;

public class Alignment {
	public final String aligned;
	public final int delta;
	public int position;
	public final int endsAt;

	public Alignment(String aligned, int delta, int endsAt, int pos) {
		this.aligned = aligned;
		this.delta = delta;
		this.endsAt = endsAt;
		this.position = pos;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Alignment) {
			Alignment o = (Alignment)other;
			return o.aligned.equals(aligned) &&
				o.delta==delta &&
				o.endsAt==endsAt &&
				o.position==position;
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
		return aligned+", d "+delta+", e "+endsAt+", p "+position;
	}
}
