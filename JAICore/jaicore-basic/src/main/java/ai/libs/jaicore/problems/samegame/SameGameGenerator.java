package ai.libs.jaicore.problems.samegame;

import java.util.Random;

public class SameGameGenerator {

	public SameGameState generate(final long seed) {
		return this.generate(15, 15, 5, 1.0, new Random(seed));
	}

	public SameGameState generate(final int rows, final int cols, final int numColors, final double fillRate, final Random random) {
		int maxPieces = (int)Math.round(rows * cols * fillRate);
		int i = 0;
		byte[][] board = new byte[rows][cols];
		for (int row = rows - 1; row >= 0 && i < maxPieces; row --) {
			for (int col = 0; col < cols && i < maxPieces; col ++, i++) {
				board[row][col] = (byte)(random.nextInt(numColors) + 1);
			}
		}
		return new SameGameState(board);
	}

}
