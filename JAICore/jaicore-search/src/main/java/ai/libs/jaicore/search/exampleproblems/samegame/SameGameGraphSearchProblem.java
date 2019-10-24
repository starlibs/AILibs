package ai.libs.jaicore.search.exampleproblems.samegame;

import org.api4.java.ai.graphsearch.problem.IGraphSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.PathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.problems.samegame.SameGameState;

public class SameGameGraphSearchProblem implements IGraphSearchWithPathEvaluationsInput<SameGameState, Pair<Integer, Integer>, Double> {

	private static final SameGameGoalPredicate GP = new SameGameGoalPredicate();

	private final SameGameGraphGenerator gg;
	private final int maxScore;

	public SameGameGraphSearchProblem(final SameGameState initState) {
		this.gg = new SameGameGraphGenerator(initState);
		this.maxScore = (int)Math.pow(initState.getNumberOfPiecesPerColor().values().stream().max((x,y) -> Integer.compare(x, y)).get() - 2, 2);
	}

	@Override
	public SameGameGraphGenerator getGraphGenerator() {
		return this.gg;
	}

	@Override
	public PathGoalTester<SameGameState, Pair<Integer, Integer>> getGoalTester() {
		return GP;
	}

	@Override
	public IPathEvaluator<SameGameState, Pair<Integer, Integer>, Double> getPathEvaluator() {
		return p -> ((double)p.getHead().getScore()) / this.maxScore;
	}
}
