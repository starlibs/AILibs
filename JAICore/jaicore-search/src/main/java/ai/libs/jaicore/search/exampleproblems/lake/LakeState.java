package ai.libs.jaicore.search.exampleproblems.lake;

public class LakeState {
	private final LakeLayout layout;
	private final int row;
	private final int col;

	public LakeState(final LakeLayout layout, final int row, final int col) {
		super();
		this.layout = layout;
		this.row = row;
		this.col = col;
	}

	public LakeLayout getLayout() {
		return this.layout;
	}

	public int getRow() {
		return this.row;
	}

	public int getCol() {
		return this.col;
	}

	public boolean isInPit() {
		return this.layout.getPits()[this.row][this.col];
	}

	public String getStringVisualization() {
		StringBuilder sb = new StringBuilder();
		int cols = this.layout.getCols();
		int rows = this.layout.getRows();
		for (int r = 0; r < rows; r++) {

			/* print grid above row */
			for (int c = 0; c < cols; c ++) {
				sb.append("+-");
			}
			sb.append("+\n");

			/* print content of row */
			for (int c = 0; c < cols; c ++) {
				sb.append("|");
				if (c == this.col && r == this.row) {
					sb.append("x");
				}
				else if (this.layout.getPits()[r][c]) {
					sb.append("*");
				}
				else {
					sb.append(" ");
				}
			}
			sb.append("+\n");
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.col;
		result = prime * result + ((this.layout == null) ? 0 : this.layout.hashCode());
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
		LakeState other = (LakeState) obj;
		if (this.col != other.col) {
			return false;
		}
		if (this.layout == null) {
			if (other.layout != null) {
				return false;
			}
		} else if (!this.layout.equals(other.layout)) {
			return false;
		}
		if (this.row != other.row) {
			return false;
		}
		return true;
	}
}
