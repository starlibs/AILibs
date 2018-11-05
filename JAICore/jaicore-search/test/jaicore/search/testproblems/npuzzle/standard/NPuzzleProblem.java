package jaicore.search.testproblems.npuzzle.standard;

public class NPuzzleProblem {

	private final int[][] board;
	private final int dim;

	public NPuzzleProblem(int[][] board) {
		this.board = board;
		this.dim = board.length;
	}

	public NPuzzleProblem(int dim, int seed) {
		this(new NPuzzleNode(dim, seed).board);
	}

	public int[][] getBoard() {
		return board;
	}

	public int getDim() {
		return dim;
	}
}
