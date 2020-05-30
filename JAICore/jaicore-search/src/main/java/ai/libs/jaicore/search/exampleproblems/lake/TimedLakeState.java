package ai.libs.jaicore.search.exampleproblems.lake;

public class TimedLakeState extends LakeState {
	private final int time;

	public TimedLakeState(final LakeLayout layout, final int row, final int col, final int time) {
		super(layout, row, col);
		this.time = time;
	}

	public int getTime() {
		return this.time;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + this.time;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		TimedLakeState other = (TimedLakeState) obj;
		return this.time == other.time;
	}
}
