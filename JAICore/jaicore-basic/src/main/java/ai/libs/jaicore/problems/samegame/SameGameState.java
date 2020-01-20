package ai.libs.jaicore.problems.samegame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.shorts.ShortRBTreeSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;

public class SameGameState {
	private final short score;
	private final byte[][] board; // 0 for empty cells, positive ints for the colors
	private final short numPieces;

	public SameGameState(final byte[][] board) {
		this.score = 0;
		this.board = board;
		short tmpNumPieces = 0;
		for (int i= 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				if (board[i][j] != 0) {
					tmpNumPieces ++;
				}
			}
		}
		this.numPieces = tmpNumPieces;
	}

	private SameGameState(final short score, final byte[][] board, final short numPieces) {
		super();
		this.score = score;
		this.board = board;
		this.numPieces = numPieces;
	}

	public SameGameState getStateAfterMove(final Collection<SameGameCell> block) {

		if (block.size() < 2) {
			throw new IllegalArgumentException("Removed blocks must have size at least 2.");
		}

		/* create a copy of the board */
		byte[][] boardCopy = new byte[this.board.length][this.board[0].length];
		for (int i = 0; i < this.board.length; i++) {
			for (int j = 0; j < this.board[i].length; j++) {
				boardCopy[i] = Arrays.copyOf(this.board[i], this.board[i].length);
			}
		}

		/* first remove all the blocks associated to the field */
		Collection<SameGameCell> removedPieces = block;
		if (removedPieces.size() < 2) {
			throw new IllegalArgumentException();
		}
		int maximumAffectedRow = 0;
		ShortSet colsToDrop = new ShortRBTreeSet();
		for (SameGameCell piece : removedPieces) {
			boardCopy[piece.getRow()][piece.getCol()] = 0;
			colsToDrop.add(piece.getCol());
			maximumAffectedRow = Math.max(maximumAffectedRow, piece.getCol());
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

		/* if a column has been cleared, move the others left */
		int offset = 0;
		for (int c = 0; c < boardCopy[0].length; c++) {
			if (boardCopy[boardCopy.length - 1][c] == 0) {
				offset ++;
			}
			else if (offset > 0) {
				for (int r = 0; r < boardCopy.length; r++) {
					boardCopy[r][c - offset] = boardCopy[r][c];
					boardCopy[r][c] = 0;
				}
			}
		}

		short newScore = (short)(this.score + (int)Math.pow(removedPieces.size() - 2.0, 2));
		short newNumPieces = (short)(this.numPieces - removedPieces.size());
		if (!isMovePossible(boardCopy)) {
			if (newNumPieces == 0) {
				newScore += 1000;
			}
			else {
				newScore -= Math.pow(newNumPieces - 2.0,2);
			}
		}
		return new SameGameState(newScore, boardCopy, newNumPieces);
	}

	public SameGameState getStateAfterMove(final byte row, final byte col) {
		return this.getStateAfterMove(this.getAllConnectedPiecesOfSameColor(row, col));
	}

	public List<SameGameCell> getAllConnectedPiecesOfSameColor(final byte row, final byte col) {
		if (this.board[row][col] == 0) {
			throw new IllegalArgumentException("There is no block in row " + row + " and col " + col);
		}
		List<SameGameCell> removed = new ArrayList<>();
		removed.add(new SameGameCell(row, col));
		this.getAllConnectedPiecesOfSameColor(row, col, removed);
		return removed;
	}

	private boolean getAllConnectedPiecesOfSameColor(final byte row, final byte col, final List<SameGameCell> countedPieces) {
		byte color = this.board[row][col];
		boolean addedOne = false;
		int colorLeft = col > 0 ? this.board[row][col - 1] : 0;
		SameGameCell leftCell = new SameGameCell(row, (byte)(col - 1));
		if (colorLeft == color && !countedPieces.contains(leftCell)) {
			countedPieces.add(leftCell);
			this.getAllConnectedPiecesOfSameColor(row, leftCell.getCol(), countedPieces);
			addedOne = true;
		}
		int colorRight = col < this.board[row].length - 1 ? this.board[row][col + 1] : 0;
		SameGameCell rightCell = new SameGameCell(row, (byte)(col + 1));
		if (colorRight == color && !countedPieces.contains(rightCell)) {
			countedPieces.add(rightCell);
			this.getAllConnectedPiecesOfSameColor(row, rightCell.getCol(), countedPieces);
			addedOne = true;
		}
		int colorUp = row > 0 ? this.board[row - 1][col] : 0;
		SameGameCell upCell = new SameGameCell((byte)(row - 1), col);
		if (colorUp == color && !countedPieces.contains(upCell)) {
			countedPieces.add(upCell);
			this.getAllConnectedPiecesOfSameColor(upCell.getRow(), col, countedPieces);
			addedOne = true;
		}
		int colorDown = row < this.board.length - 1 ? this.board[row + 1][col] : 0;
		SameGameCell downCell = new SameGameCell((byte)(row + 1), col);
		if (colorDown == color && !countedPieces.contains(downCell)) {
			countedPieces.add(downCell);
			this.getAllConnectedPiecesOfSameColor(downCell.getRow(), col, countedPieces);
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

	public Collection<Collection<SameGameCell>> getBlocksOfPieces() {

		/* Collect one representative for each block */
		List<Collection<SameGameCell>> identifiedBlocks = new ArrayList<>();
		Map<SameGameCell, Integer> blocksOfCells = new HashMap<>();
		SameGameCell[][] cellObjects = new SameGameCell[this.board.length][this.board[0].length];
		byte lastRow = -1;
		byte lastCol;
		IntList indicesToRemove = new IntArrayList();
		for (byte row = 0; row < this.board.length; row ++) {
			lastCol = -1;
			for (byte col = 0; col < this.board[row].length; col ++) {
				byte color = this.board[row][col];
				if (color != 0) {
					SameGameCell cell = new SameGameCell(row, col);
					cellObjects[row][col] = cell;

					/* if the cell has the same color as the cell above, put it into the same group */
					if (row > 0 && this.board[lastRow][col] == color) {
						int topBlockId = blocksOfCells.get(cellObjects[lastRow][col]);
						blocksOfCells.put(cell, topBlockId);
						identifiedBlocks.get(topBlockId).add(cell);

						/* if the cell has ALSO the same value as the left neighbor, merge the groups (unless those groups already HAVE been merged) */
						if (col > 0 && this.board[row][lastCol] == color) {
							int leftBlockId = blocksOfCells.get(cellObjects[row][lastCol]); // gets the id of the block of the piece on the left
							if (identifiedBlocks.get(leftBlockId) != identifiedBlocks.get(topBlockId)) {
								identifiedBlocks.get(topBlockId).addAll(identifiedBlocks.get(leftBlockId)); // adds all the elements of the left block to the top block
								identifiedBlocks.set(leftBlockId, identifiedBlocks.get(topBlockId)); // sets the left block to be equal with the top block
								indicesToRemove.add(leftBlockId);
							}
						}
					}

					/* else, if the cell has ONLY the same value as its left neighbor, joint them */
					else if (col > 0 && this.board[row][lastCol] == color) {
						int blockId = blocksOfCells.get(cellObjects[row][lastCol]);
						blocksOfCells.put(cell, blockId);
						identifiedBlocks.get(blockId).add(cell);
					}

					/* otherwise, the cell cannot be merged with left or top and, hence, opens a new block */
					else {
						int blockId = identifiedBlocks.size();
						Collection<SameGameCell> block = new ArrayList<>();
						block.add(cell);
						blocksOfCells.put(cell, blockId);
						identifiedBlocks.add(block);
					}
				}
				lastCol = col;
			}
			lastRow = row;
		}
		for (int r : indicesToRemove.stream().sorted((i1,i2) -> Integer.compare(i2, i1)).collect(Collectors.toList())) {
			identifiedBlocks.remove(r);
		}
		return identifiedBlocks;
	}

	public short getScore() {
		return this.score;
	}

	public int getNumPieces() {
		return this.numPieces;
	}

	public static boolean isMovePossible(final byte[][] board) {
		for (int row = 0; row < board.length; row ++) {
			for (int col = 0; col < board[row].length; col ++) {
				if (canCellBeSelected(board, row, col)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isMovePossible() {
		return isMovePossible(this.board);
	}

	public boolean canCellBeSelected(final int row, final int col) {
		return canCellBeSelected(this.board, row, col);
	}

	public static boolean canCellBeSelected(final byte[][] board, final int row, final int col) {
		byte color = board[row][col];
		if (color == 0) {
			return false;
		}
		if (row > 0 && board[row - 1][col] == color) {
			return true;
		}
		if (row < board.length - 1 && board[row + 1][col] == color) {
			return true;
		}
		if (col < board[row].length - 1 && board[row][col + 1] == color) {
			return true;
		}
		return (col > 0 && board[row][col - 1] == color);
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
		result = prime * result + Arrays.deepHashCode(this.board);
		result = prime * result + this.score;
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
		if (!Arrays.deepEquals(this.board, other.board)) {
			return false;
		}
		return this.score == other.score;
	}
}
