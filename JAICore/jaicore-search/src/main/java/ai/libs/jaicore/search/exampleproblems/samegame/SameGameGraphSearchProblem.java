package ai.libs.jaicore.search.exampleproblems.samegame;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.problems.samegame.SameGameCell;
import ai.libs.jaicore.problems.samegame.SameGameState;

public class SameGameGraphSearchProblem implements IPathSearchWithPathEvaluationsInput<SameGameNode, SameGameCell, Double> {

	private static final SameGameGoalPredicate GP = new SameGameGoalPredicate();

	private final SameGameGraphGenerator gg;
	private final boolean maximize;
	private final int maxScore;
	private final boolean relativeScores;

	public SameGameGraphSearchProblem(final SameGameState initState) {
		this(initState, true);
	}

	public SameGameGraphSearchProblem(final SameGameState initState, final boolean maximize) {
		this(initState, maximize, false);
	}

	public SameGameGraphSearchProblem(final SameGameState initState, final boolean maximize, final boolean relativeScores) {
		this.gg = new SameGameGraphGenerator(initState);
		this.maxScore = (int)Math.pow(initState.getNumberOfPiecesPerColor().values().stream().max(Integer::compare).get() - 2.0, 2);
		this.relativeScores = relativeScores;
		this.maximize = maximize;
	}

	@Override
	public SameGameGraphGenerator getGraphGenerator() {
		return this.gg;
	}

	@Override
	public IPathGoalTester<SameGameNode, SameGameCell> getGoalTester() {
		return GP;
	}

	@Override
	public IPathEvaluator<SameGameNode, SameGameCell, Double> getPathEvaluator() {
		return p -> (this.maximize ? 1 : (-1)) * ((double)p.getHead().getScore()) / (this.relativeScores ? this.maxScore : 1);
	}

	public int getMaxScore() {
		return this.maxScore;
	}
}
