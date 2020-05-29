package ai.libs.jaicore.search.exampleproblems.racetrack;

public class RacetrackState {
	private final int x;
	private final int y;
	private final int hSpeed;
	private final int vSpeed;
	private final boolean finished;
	private final boolean lastSuccess;
	private final boolean crashed;

	public RacetrackState(final int x, final int y, final int hSpeed, final int vSpeed, final boolean lastSuccess, final boolean finished, final boolean crashed) {
		super();
		this.x = x;
		this.y = y;
		this.hSpeed = hSpeed;
		this.vSpeed = vSpeed;
		this.lastSuccess = lastSuccess;
		this.finished = finished;
		this.crashed = crashed;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public int getvSpeed() {
		return this.vSpeed;
	}

	public int gethSpeed() {
		return this.hSpeed;
	}

	public boolean isFinished() {
		return this.finished;
	}

	public boolean isLastSuccess() {
		return this.lastSuccess;
	}

	public boolean isCrashed() {
		return this.crashed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.hSpeed;
		result = prime * result + this.vSpeed;
		result = prime * result + this.x;
		result = prime * result + this.y;
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
		RacetrackState other = (RacetrackState) obj;
		if (this.hSpeed != other.hSpeed) {
			return false;
		}
		if (this.vSpeed != other.vSpeed) {
			return false;
		}
		if (this.x != other.x) {
			return false;
		}
		return this.y == other.y;
	}

	@Override
	public String toString() {
		return this.x + "/" + this.y + " (" + this.hSpeed + "/" + this.vSpeed + ")[" + this.lastSuccess + "]";
	}
}
