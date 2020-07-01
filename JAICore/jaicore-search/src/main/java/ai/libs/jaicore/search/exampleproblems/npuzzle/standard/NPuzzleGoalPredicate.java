package ai.libs.jaicore.search.exampleproblems.npuzzle.standard;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;

import ai.libs.jaicore.problems.npuzzle.NPuzzleProblem;

public class NPuzzleGoalPredicate implements INodeGoalTester<NPuzzleProblem, String> {

	public NPuzzleGoalPredicate() {
		super();
	}

	@Override
	public boolean isGoal(final NPuzzleProblem n) {
		int[][] board = n.getBoard();
		int height = board.length;
		int width = board[0].length;
		if (board[0][0] != 0) {
			return false;
		} else {
			int sol = 0;
			for (int c = 0; c < width; c++) {
				for (int r = 0; r < height; r++) {
					if (board[r][c] != sol) {
						return false;
					}
					sol++;
				}
			}
			return true;
		}
	}
}
