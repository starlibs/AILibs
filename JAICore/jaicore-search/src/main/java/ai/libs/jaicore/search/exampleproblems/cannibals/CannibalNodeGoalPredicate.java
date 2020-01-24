package ai.libs.jaicore.search.exampleproblems.cannibals;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;

import ai.libs.jaicore.problems.cannibals.CannibalProblem;

public class CannibalNodeGoalPredicate implements INodeGoalTester<CannibalProblem, String> {

	@Override
	public boolean isGoal(final CannibalProblem n) {
		return n.isWon();
	}
}
