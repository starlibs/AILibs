package ai.libs.jaicore.search.exampleproblems.racetrack;

public class RacetrackAction {

	private final int hAcceleration;
	private final int vAcceleration;

	public RacetrackAction(final int hAcceleration, final int vAcceleration) {
		super();
		this.hAcceleration = hAcceleration;
		this.vAcceleration = vAcceleration;
	}

	public int gethAcceleration() {
		return this.hAcceleration;
	}

	public int getvAcceleration() {
		return this.vAcceleration;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.hAcceleration;
		result = prime * result + this.vAcceleration;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		RacetrackAction other = (RacetrackAction) obj;
		if (this.hAcceleration != other.hAcceleration) {
			return false;
		}
		return this.vAcceleration == other.vAcceleration;
	}

	@Override
	public String toString() {
		return "acceleration(" + this.hAcceleration + ", vAcceleration=" + this.vAcceleration + ")";
	}
}
