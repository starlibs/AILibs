package ai.libs.jaicore.search.testproblems.npuzzle.standard;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;

import ai.libs.jaicore.problems.npuzzle.NPuzzleState;

public class NPuzzleGoalPredicate implements INodeGoalTester<NPuzzleState, String> {

	private final int dimension;

	public NPuzzleGoalPredicate(final int dimension) {
		super();
		this.dimension = dimension;
	}

	@Override
	public boolean isGoal(final NPuzzleState n) {
		int[][] board = n.getBoard();
		if (board[this.dimension - 1][this.dimension - 1] != 0) {
			return false;
		} else {
			int sol = 1;
			for (int i = 0; i < this.dimension; i++) {
				for (int j = 0; j < this.dimension; j++) {
					if (i != this.dimension - 1 && j != this.dimension - 1 && board[i][j] != sol) {
						return false;
					}
					sol++;
				}
			}
			return true;
		}
	}
}
