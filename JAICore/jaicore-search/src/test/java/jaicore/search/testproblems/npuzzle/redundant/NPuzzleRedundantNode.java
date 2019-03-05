package jaicore.search.testproblems.npuzzle.redundant;

import jaicore.testproblems.npuzzle.NPuzzleState;

public class NPuzzleRedundantNode extends NPuzzleState {

	String moves;

	/**
	 * Constructor for a NPuzzleNode which creates a NPuzzleNode with complete
	 * randomly distributed numbers.
	 * @param dim
	 * 		The dimension of the board.
	 */
	public NPuzzleRedundantNode(final int dim) {
		super(dim);

		this.moves = "";
	}

	/**
	 * Constructor for a NPuzzleNode which creates a NPuzzleNode.
	 * The board configuration starts with the targetconfiguration and shuffels the tiles afterwards.
	 * @param dim
	 * 			The dimension of the board.
	 * @param perm
	 * 			The number of moves which should be made before starting the search.
	 * 			This number is hardcoded to at least 1.
	 */
	public NPuzzleRedundantNode(final int dim, final int perm) {
		super(dim, perm);
		this.moves = "";
	}

	/**
	 * Constructor for a NPuzzleNode in which the board is already given.
	 * @param board
	 * 			The board configuration for this node
	 * @param emptyX
	 * 			The empty space on the x-axis.
	 * @param emptyY
	 * 			The empty space on the y-axis.
	 */
	public NPuzzleRedundantNode(final int [][] board, final int emptyX, final int emptyY, final String string) {
		super(board, emptyX, emptyY);
		this.moves += string;
	}

	public String getMoves() {
		return this.moves;
	}

}
