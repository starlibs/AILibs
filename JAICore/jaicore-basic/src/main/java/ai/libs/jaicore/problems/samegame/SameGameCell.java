package ai.libs.jaicore.problems.samegame;

public class SameGameCell {
	private final byte row;
	private final byte col;

	public SameGameCell(final byte row, final byte col) {
		super();
		this.row = row;
		this.col = col;
	}

	public byte getCol() {
		return this.col;
	}

	public byte getRow() {
		return this.row;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.col;
		result = prime * result + this.row;
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
		SameGameCell other = (SameGameCell) obj;
		if (this.col != other.col) {
			return false;
		}
		return this.row == other.row;
	}

	@Override
	public String toString() {
		return "SameGameCell [row=" + this.row + ", col=" + this.col + "]";
	}
}
