package ai.libs.jaicore.search.exampleproblems.lake;

import java.util.Arrays;

public class LakeLayout {
	private final int rows;
	private final int cols;
	private final boolean[][] pits;

	public LakeLayout(final int rows, final int cols, final boolean[][] pits) {
		super();
		this.rows = rows;
		this.cols = cols;
		this.pits = pits;
	}

	public int getRows() {
		return this.rows;
	}

	public int getCols() {
		return this.cols;
	}

	public boolean[][] getPits() {
		return this.pits;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.cols;
		result = prime * result + Arrays.hashCode(this.pits);
		result = prime * result + this.rows;
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
		LakeLayout other = (LakeLayout) obj;
		if (this.cols != other.cols) {
			return false;
		}
		if (!Arrays.equals(this.pits, other.pits)) {
			return false;
		}
		return (this.rows == other.rows);
	}
}
