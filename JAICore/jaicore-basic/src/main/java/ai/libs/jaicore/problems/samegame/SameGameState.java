package ai.libs.jaicore.problems.samegame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ai.libs.jaicore.basic.sets.Pair;

public class SameGameState {
	private final int score;
	private final byte[][] board; // 0 for empty cells, positive ints for the colors
	private final int numPieces;
	private final List<Integer> boardHashHistory; // this is to ensure that we search a tree.

	public SameGameState(final byte[][] board) {
		this.score = 0;
		this.board = board;
		int tmpNumPieces = 0;
		for (int i= 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				if (board[i][j] != 0) {
					tmpNumPieces ++;
				}
			}
		}
		this.numPieces = tmpNumPieces;
		this.boardHashHistory = Arrays.asList(board.hashCode());
	}

	private SameGameState(final int score, final byte[][] board, final int numPieces, final List<Integer> boardHashHistoryOfParent) {
		super();
		this.score = score;
		this.board = board;
		this.numPieces = numPieces;
		List<Integer> newHistory = new ArrayList<>(boardHashHistoryOfParent);
		newHistory.add(board.hashCode());
		this.boardHashHistory = newHistory;
	}

	public SameGameState getStateAfterMove(final Collection<Pair<Integer, Integer>> block) {

		if (block.size() < 2) {
			throw new IllegalArgumentException("Removed blocks must have size at least 2.");
		}

		/* create a copy of the board */
		byte[][] boardCopy = new byte[this.board.length][this.board[0].length];
		for (int i = 0; i < this.board.length; i++) {
			for (int j = 0; j < this.board[i].length; j++) {
				boardCopy[i][j] = this.board[i][j];
			}
		}

		/* first remove all the blocks associated to the field */
		Collection<Pair<Integer, Integer>> removedPieces = block;
		if (removedPieces.size() < 2) {
			throw new IllegalArgumentException();
		}
		int maximumAffectedRow = 0;
		Set<Integer> colsToDrop = new HashSet<>();
		for (Pair<Integer, Integer> piece : removedPieces) {
			boardCopy[piece.getX()][piece.getY()] = 0;
			colsToDrop.add(piece.getY());
			maximumAffectedRow = Math.max(maximumAffectedRow, piece.getX());
		}

		/* now drop all the blocks in the affected columns */
		for (int c : colsToDrop) {
			int fallHeightInColumn = 0;
			boolean touchedFirstEmpty = false;
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
		return new SameGameState(this.score + (int)Math.pow(removedPieces.size() - 2, 2), boardCopy, this.numPieces - removedPieces.size(), this.boardHashHistory);
	}

	public SameGameState getStateAfterMove(final int row, final int col) {
		return this.getStateAfterMove(this.getAllConnectedPiecesOfSameColor(row, col));
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

	public byte[][] getBoard() {
		return this.board;
	}

	public int getNumRows() {
		return this.board.length;
	}

	public int getNumCols() {
		return this.board[0].length;
	}

	public Collection<Collection<Pair<Integer, Integer>>> getBlocksOfPieces() {

		/* Collect one representative for each block */
		Map<Integer, Collection<Pair<Integer, Integer>>> identifiedBlocks = new HashMap<>();
		Set<Pair<Integer, Integer>> consideredBlocks = new HashSet<>();
		int blockId = 0;
		for (int row = 0; row < this.board.length; row ++) {
			for (int col = 0; col < this.board[row].length; col ++) {
				boolean isNewBlock = false;
				if (this.board[row][col] == 0 || consideredBlocks.contains(new Pair<>(row, col))) {
					continue;
				}
				for (Pair<Integer, Integer> pieceInBlock : this.getAllConnectedPiecesOfSameColor(row, col)) {
					consideredBlocks.add(pieceInBlock);
					if (!isNewBlock) {
						isNewBlock = true;
						identifiedBlocks.put(blockId, new HashSet<>());
					}
					identifiedBlocks.get(blockId).add(pieceInBlock);
				}
				if (isNewBlock) {
					blockId ++;
				}
			}
		}
		return identifiedBlocks.values();
	}

	public int getScore() {
		return this.score * -1;
	}

	public int getNumPieces() {
		return this.numPieces;
	}

	public Map<Integer, Integer> getNumberOfPiecesPerColor() {
		Map<Integer, Integer> map = new HashMap<>();
		for (int row = 0; row < this.board.length; row ++) {
			for (int col = 0; col < this.board[row].length; col ++) {
				int color = this.board[row][col];
				if (color == 0) {
					continue;
				}
				map.put(color, map.computeIfAbsent(color, c -> 0) + 1);
			}
		}
		return map;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.boardHashHistory == null) ? 0 : this.boardHashHistory.hashCode());
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
		SameGameState other = (SameGameState) obj;
		if (this.boardHashHistory == null) {
			if (other.boardHashHistory != null) {
				return false;
			}
		} else if (!this.boardHashHistory.equals(other.boardHashHistory)) {
			return false;
		}
		return true;
	}
}
