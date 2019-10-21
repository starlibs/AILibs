package ai.libs.jaicore.problems.samegame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ai.libs.jaicore.basic.sets.Pair;

public class SameGameState {
	private final int score;
	private final int[][] board; // 0 for empty cells, positive ints for the colors

	public SameGameState(final int[][] board) {
		this(0, board);
	}

	public SameGameState(final int score, final int[][] board) {
		super();
		this.score = score;
		this.board = board;
	}

	public SameGameState getStateAfterMove(final int row, final int col) {

		/* create a copy of the board */
		int[][] boardCopy = new int[this.board.length][this.board[0].length];
		for (int i = 0; i < this.board.length; i++) {
			for (int j = 0; j < this.board[i].length; j++) {
				boardCopy[i][j] = this.board[i][j];
			}
		}

		/* first remove all the blocks associated to the field */
		List<Pair<Integer, Integer>> removedBlocks = this.getAllConnectedPiecesOfSameColor(row, col);
		if (removedBlocks.size() < 2) {
			throw new IllegalArgumentException();
		}
		int maximumAffectedRow = 0;
		Set<Integer> colsToDrop = new HashSet<>();
		for (Pair<Integer, Integer> block : removedBlocks) {
			boardCopy[block.getX()][block.getY()] = 0;
			colsToDrop.add(block.getY());
			maximumAffectedRow = Math.max(maximumAffectedRow, block.getX());
		}

		/* now drop all the blocks in the affected columns */
		for (int c : colsToDrop) {
			int fallHeightInColumn = 0;
			boolean touchedFirstEmpty = false;
			boolean touchedAnyNonEmpty = false;
			for (int r = this.board.length - 1; r >= 0; r --) {
				if (boardCopy[r][c] == 0) {
					fallHeightInColumn ++;
					touchedFirstEmpty = true;
				}
				else {
					if (touchedFirstEmpty) {
						boardCopy[r + fallHeightInColumn][c] = boardCopy[r][c];
						boardCopy[r][c] = 0;
					}
				}
			}
		}

		/* move non-empty columns */
		for (int c = 0; c < boardCopy[0].length; c++) {
			boolean colapsCol = boardCopy[boardCopy.length - 1][c] == 0;
			if (colapsCol) {
				for (int cp = c + 1; cp < boardCopy[0].length; cp ++) {
					for (int r = 0; r < this.board.length; r ++) {
						boardCopy[r][cp - 1] = boardCopy[r][cp];
						boardCopy[r][cp] = 0;
					}
				}
			}
		}

		return new SameGameState(this.score + (int)Math.pow(removedBlocks.size() - 2, 2), boardCopy);
	}

	public List<Pair<Integer, Integer>> getAllConnectedPiecesOfSameColor(final int row, final int col) {
		if (this.board[row][col] == 0) {
			throw new IllegalArgumentException("There is no block in row " + row + " and col " + col);
		}
		Set<Pair<Integer, Integer>> removed = new HashSet<>();
		removed.add(new Pair<>(row, col));
		this.getAllConnectedPiecesOfSameColor(row, col, removed);
		return new ArrayList<>(removed);
	}

	private boolean getAllConnectedPiecesOfSameColor(final int row, final int col, final Set<Pair<Integer, Integer>> countedPieces) {
		int color = this.board[row][col];
		boolean addedOne = false;
		int colorLeft = col > 0 ? this.board[row][col - 1] : 0;
		if (colorLeft == color && !countedPieces.contains(new Pair<>(row, col - 1))) {
			countedPieces.add(new Pair<>(row, col - 1));
			this.getAllConnectedPiecesOfSameColor(row, col - 1, countedPieces);
			addedOne = true;
		}
		int colorRight = col < this.board[row].length - 1 ? this.board[row][col + 1] : 0;
		if (colorRight == color && !countedPieces.contains(new Pair<>(row, col + 1))) {
			countedPieces.add(new Pair<>(row, col + 1));
			this.getAllConnectedPiecesOfSameColor(row, col + 1, countedPieces);
			addedOne = true;
		}
		int colorUp = row > 0 ? this.board[row - 1][col] : 0;
		if (colorUp == color && !countedPieces.contains(new Pair<>(row - 1, col))) {
			countedPieces.add(new Pair<>(row - 1, col));
			this.getAllConnectedPiecesOfSameColor(row - 1, col, countedPieces);
			addedOne = true;
		}
		int colorDown = row < this.board.length - 1 ? this.board[row + 1][col] : 0;
		if (colorDown == color && !countedPieces.contains(new Pair<>(row + 1, col))) {
			countedPieces.add(new Pair<>(row + 1, col));
			this.getAllConnectedPiecesOfSameColor(row + 1, col, countedPieces);
			addedOne = true;
		}
		return addedOne;
	}

	public String getBoardAsString() {
		StringBuilder sb = new StringBuilder();
		for (int row = 0; row < this.board.length; row ++) {
			for (int col = 0; col < this.board[row].length; col ++) {
				sb.append(this.board[row][col]);
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
