package ai.libs.jaicore.problems.npuzzle;

import java.util.Collections;

import ai.libs.jaicore.basic.sets.SetUtil;
import it.unimi.dsi.fastutil.ints.IntList;

public class NPuzzleProblemGenerator {

	public NPuzzleProblem generate(final int squareSize) {
		return this.generate(squareSize, squareSize);
	}

	public NPuzzleProblem generate(final int numRows, final int numCols) {
		IntList range = SetUtil.range(numRows * numCols);
		Collections.shuffle(range);
		int[][] board = new int[numRows][numCols];
		int i = 0;
		for (int r = 0; r < numRows; r++) {
			for (int c = 0; c < numCols; c++) {
				board[r][c] = range.getInt(i++);
			}
		}
		return new NPuzzleProblem(board);
	}
}
