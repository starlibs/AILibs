package jaicore.testproblems.npuzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A node for the normal n-Puzzleproblem.
 * Every node contains the current board configuration as an 2D-Array of integer.
 * The empty space is indicated by an Integer with value 0.
 *
 * @author jkoepe
 */
public class NPuzzleState {

	// board configuration and empty space
	protected int[][] board;
	protected int emptyX;
	protected int emptyY;

	/**
	 * Constructor for a NPuzzleNode which creates a NPuzzleNode with complete
	 * randomly distributed numbers.
	 *
	 * @param dim
	 *            The dimension of the board.
	 */
	public NPuzzleState(final int dim) {
		this(dim, 0);
	}

	/**
	 * Constructor for a NPuzzleNode which creates a NPuzzleNode.
	 * The board configuration starts with the targetconfiguration and shuffels the tiles afterwards.
	 *
	 * @param dim
	 *            The dimension of the board.
	 * @param perm
	 *            The number of moves which should be made before starting the search.
	 *            This number is hardcoded to at least 1.
	 */
	public NPuzzleState(final int dim, final int seed) {
		this.board = new int[dim][dim];
		List<Integer> numbers = new ArrayList<>();
		for (int i = 0; i < dim * dim; i++) {
			numbers.add(i);
		}
		Collections.shuffle(numbers, new Random(seed));
		int c = 0;
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				this.board[i][j] = numbers.get(c++);
			}
		}
	}

	/**
	 * Constructor for a NPuzzleNode in which the board is already given.
	 *
	 * @param board
	 *            The board configuration for this node
	 * @param emptyX
	 *            The empty space on the x-axis.
	 * @param emptyY
	 *            The empty space on the y-axis.
	 *
	 * @param noMoves
	 *            The number of already done moves.
	 */
	public NPuzzleState(final int[][] board, final int emptyX, final int emptyY) {
		this.board = board;
		this.emptyX = emptyX;
		this.emptyY = emptyY;
	}

	public int[][] getBoard() {
		return this.board;
	}

	public int getEmptyX() {
		return this.emptyX;
	}

	public int getEmptyY() {
		return this.emptyY;
	}

	/**
	 * Returns a graphical version of the board configuration.
	 * Works best if there is no number with two or more digits.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (int j = 0; j < this.board.length; j++) {
			sb.append("----");
		}
		sb.append("\n");

		for (int i = 0; i < this.board.length; i++) {
			sb.append("| ");
			for (int j = 0; j < this.board.length; j++) {
				sb.append(this.board[i][j] + " | ");
			}
			sb.append("\n");
			for (int j = 0; j < this.board.length; j++) {
				sb.append("----");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Returns the number of wrongly placed tiles
	 *
	 * @return
	 * 		The number of wrongly placed tiles.
	 */
	public int getNumberOfWrongTiles() {
		int wrongTiles = 0;
		int x = 1;
		for (int i = 0; i < this.board.length; i++) {
			for (int j = 0; j < this.board.length; j++) {
				if (i == this.board.length - 1 && j == this.board.length - 1) {
					x = 0;
				}

				if (x != this.board[i][j]) {
					wrongTiles++;
				}

				x++;
			}
		}

		return wrongTiles;
	}

	/**
	 * Returns the steps which are minimal need to reach a goal state
	 *
	 * @return
	 * 		The number of steps.
	 */
	public double getDistance() {
		double d = 0.0;
		for (int i = 0; i < this.board.length; i++) {
			for (int j = 0; j < this.board.length; j++) {
				int tile = this.board[i][j];
				double x = (double) tile / this.board.length;
				int y = tile % this.board.length - 1;
				if (x % 1 == 0) {
					x--;
				}
				x = Math.floor(x);
				if (y < 0) {
					y = this.board.length - 1;
				}

				if (tile == 0) {
					continue;
				}
				double h1 = Math.abs(i - x);
				double h2 = Math.abs(j - y);
				double d1 = h1 + h2;
				d += d1;
			}
		}
		return d;
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
		if (!Arrays.deepEquals(this.board, other.board)) {
			return false;
		}
		if (this.emptyX != other.emptyX) {
			return false;
		}
		return this.emptyY == other.emptyY;
	}
}
