package jaicore.search.testproblems.npuzzle.parentDiscarding;

import java.util.Arrays;

import jaicore.search.testproblems.npuzzle.standard.NPuzzleNode;

public class PDPuzzleNode extends NPuzzleNode {

	public PDPuzzleNode(int[][] board, int emptyX, int emptyY, int numberOfMoves) {
		super(board, emptyX, emptyY, numberOfMoves);
	}

	
	/* (non-Javadoc)
	 * @see jaicore.search.algorithms.standard.npuzzle.NPuzzleNode#getDistance()
	 */
	@Override
	public double getDistance() {
	
		return Math.abs((board.length-1)-emptyX)+Math.abs((board.length-1)-emptyY);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(board);
//		result = prime * result + Arrays.hashCode(board);
		result = prime * result + emptyX;
		result = prime * result + emptyY;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NPuzzleNode other = (NPuzzleNode) obj;
		if (emptyX != other.getEmptyX())
			return false;
		if (emptyY != other.getEmptyY())
			return false;
		return true;
	}

}
