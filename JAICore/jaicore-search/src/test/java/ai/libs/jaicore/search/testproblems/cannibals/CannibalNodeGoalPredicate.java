package ai.libs.jaicore.search.testproblems.cannibals;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeGoalTester;

import ai.libs.jaicore.testproblems.cannibals.CannibalProblem;

public class CannibalNodeGoalPredicate implements NodeGoalTester<CannibalProblem, String> {

	@Override
	public boolean isGoal(final CannibalProblem n) {
		return n.isWon();
	}
}
