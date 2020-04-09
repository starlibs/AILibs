package ai.libs.jaicore.search.exampleproblems.sailing;

public class SailingState {
	private final int row;
	private final int col;
	private final SailingMove wind; // indicates from where one perceives the wind

	public SailingState(final int row, final int col, final SailingMove wind) {
		super();
		this.row = row;
		this.col = col;
		this.wind = wind;
	}

	public int getRow() {
		return this.row;
	}

	public int getCol() {
		return this.col;
	}

	public SailingMove getWind() {
		return this.wind;
	}

	@Override
	public String toString() {
		return this.row + "/" + this.col + "/" + this.wind;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.col;
		result = prime * result + this.row;
		result = prime * result + ((this.wind == null) ? 0 : this.wind.hashCode());
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
		SailingState other = (SailingState) obj;
		if (this.col != other.col) {
			return false;
		}
		if (this.row != other.row) {
			return false;
		}
		if (this.wind != other.wind) {
			return false;
		}
		return true;
	}
}
