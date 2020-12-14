package ai.libs.jaicore.search.exampleproblems.samegame;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.problems.samegame.SameGameCell;
import ai.libs.jaicore.problems.samegame.SameGameState;

public class SameGamePathEvaluator implements IPathEvaluator<SameGameNode, SameGameCell, Double> {

	private final boolean maximize;
	private final int minScore;
	private final int maxScore;
	private final boolean relativeScores;

	public SameGamePathEvaluator(final SameGameState initState, final boolean maximize, final boolean relativeScores) {
		this.minScore = -10000;
		this.maxScore = (int) Math.pow(initState.getNumberOfPiecesPerColor().values().stream().max(Integer::compare).get() - 2.0, 2);
		this.relativeScores = relativeScores;
		this.maximize = maximize;
	}

	@Override
	public Double evaluate(final ILabeledPath<SameGameNode, SameGameCell> path) throws PathEvaluationException, InterruptedException {
		double unitVal = ((double) path.getHead().getScore() - this.minScore) / (this.relativeScores ? (this.maxScore - this.minScore) : 1);
		return this.maximize ? unitVal : (1 - unitVal);
	}

	public double getOriginalScoreFromRelativeScore(final double relativeScore) {
		double relOriginalScore = this.maximize ? relativeScore : (1-relativeScore);
		return relOriginalScore * (this.maxScore - this.minScore) + this.minScore;
	}

	public boolean isMaximize() {
		return this.maximize;
	}

	public int getMinScore() {
		return this.minScore;
	}

	public int getMaxScore() {
		return this.maxScore;
	}

	public boolean isRelativeScores() {
		return this.relativeScores;
	}

}
