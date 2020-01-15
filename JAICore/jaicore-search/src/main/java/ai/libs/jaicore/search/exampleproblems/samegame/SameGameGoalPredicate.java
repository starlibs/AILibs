package ai.libs.jaicore.search.exampleproblems.samegame;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;

import ai.libs.jaicore.problems.samegame.SameGameCell;

public class SameGameGoalPredicate implements INodeGoalTester<SameGameNode, SameGameCell> {

	@Override
	public boolean isGoal(final SameGameNode node) {
		return node.isGoalState();
	}

}
