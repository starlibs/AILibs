package ai.libs.jaicore.search.exampleproblems.samegame;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeGoalTester;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.problems.samegame.SameGameState;

public class SameGameGoalPredicate implements NodeGoalTester<SameGameState, Pair<Integer, Integer>> {

	@Override
	public boolean isGoal(final SameGameState node) {
		return node.getBlocksOfPieces().stream().allMatch(b -> b.size() == 1);
	}

}
