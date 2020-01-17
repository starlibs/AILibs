package ai.libs.jaicore.problems.samegame;

import java.util.Collection;
import java.util.stream.Collectors;

public class SameGameStateChecker {
	public boolean checkThatEveryPieceIsInExactlyOneBlock(final SameGameState s) {

		Collection<Collection<SameGameCell>> blocks = s.getBlocksOfPieces();
		byte[][] board = s.getBoard();
		for (byte row = 0; row < board.length; row ++) {
			for (byte col = 0; col < board[row].length; col ++) {
				if (board[row][col] != 0) {
					SameGameCell cell = new SameGameCell(row, col);
					int matchingBlocks = blocks.stream().filter(b -> b.contains(cell)).collect(Collectors.toList()).size();
					if (matchingBlocks != 1) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
