package ai.libs.jaicore.problems.npuzzle;

import ai.libs.jaicore.basic.sets.IntCoordinates;

public class NPuzzleProblem {

	private final int[][] board;
	private final int emptyFieldRow;
	private final int emptyFieldCol;

	public static IntCoordinates getEmptyCell(final int[][] board) {
		for (int r = 0; r < board.length; r++) {
			for (int c = 0; c < board[r].length; c++) {
				if (board[r][c] == 0) {
					return new IntCoordinates(c, r);
				}
			}
		}
		return null;
	}

	public NPuzzleProblem(final int[][] board) {
		this(board, getEmptyCell(board));
	}

	public NPuzzleProblem(final int[][] board, final IntCoordinates emptyCellPosition) {
		this(board, emptyCellPosition.getY(), emptyCellPosition.getX());
	}

	public NPuzzleProblem(final int[][] board, final int emptyFieldRow, final int emptyFieldCol) {
		this.board = board;
		this.emptyFieldCol = emptyFieldCol;
		this.emptyFieldRow = emptyFieldRow;
	}

	public int[][] getBoard() {
		return this.board;
	}

	public int getEmptyFieldRow() {
		return this.emptyFieldRow;
	}

	public int getEmptyFieldCol() {
		return this.emptyFieldCol;
	}

	/**
	 * Returns a graphical version of the board configuration. Works best if there is no number with two or more digits.
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
	 * @return The number of wrongly placed tiles.
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
	 * @return The number of steps.
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
}
