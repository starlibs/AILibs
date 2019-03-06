package jaicore.search.testproblems.npuzzle.parentdiscarding;

import java.util.Arrays;

import jaicore.testproblems.npuzzle.NPuzzleState;

public class PDPuzzleNode extends NPuzzleState {

	public PDPuzzleNode(final int[][] board, final int emptyX, final int emptyY) {
		super(board, emptyX, emptyY);
	}

	/* (non-Javadoc)
	 * @see jaicore.search.algorithms.standard.npuzzle.NPuzzleNode#getDistance()
	 */
	@Override
	public double getDistance() {
		return (double) Math.abs((this.board.length - 1) - this.emptyX) + Math.abs((this.board.length - 1) - this.emptyY);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(this.board);
		result = prime * result + this.emptyX;
		result = prime * result + this.emptyY;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
		NPuzzleState other = (NPuzzleState) obj;
		if (this.emptyX != other.getEmptyX()) {
			return false;
		}
		return this.emptyY == other.getEmptyY();
	}

}
