package jaicore.testproblems.npuzzle;

public class NPuzzleProblem {

	private final int[][] board;
	private final int dim;

	public NPuzzleProblem(final int[][] board) {
		this.board = board;
		this.dim = board.length;
	}

	public NPuzzleProblem(final int dim, final int seed) {
		this(new NPuzzleState(dim, seed).board);
	}

	public int[][] getBoard() {
		return this.board;
	}

	public int getDim() {
		return this.dim;
	}
}
