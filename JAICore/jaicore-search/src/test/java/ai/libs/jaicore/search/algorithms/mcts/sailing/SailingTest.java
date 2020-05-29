package ai.libs.jaicore.search.algorithms.mcts.sailing;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;

import ai.libs.jaicore.search.algorithms.mcts.MCTSLearningSuccessTester;
import ai.libs.jaicore.search.exampleproblems.sailing.SailingMDP;
import ai.libs.jaicore.search.exampleproblems.sailing.SailingMove;
import ai.libs.jaicore.search.exampleproblems.sailing.SailingState;
import ai.libs.jaicore.search.probleminputs.IMDP;
import ai.libs.jaicore.search.probleminputs.MDPUtils;

public class SailingTest extends MCTSLearningSuccessTester<SailingState, SailingMove> {

	private static final int SIZE = 50;

	@Override
	public double getGamma() {
		return .999;
	}

	@Override
	public IMDP<SailingState, SailingMove, Double> getMDP() {
		return new SailingMDP(SIZE, SIZE, 0, 0, SIZE - 1, SIZE - 1, SailingMove.NW, MDPUtils.getTimeHorizon(this.getGamma(), this.getEpsilon()));
	}

	@Override
	public boolean isSuccess(final IEvaluatedPath<SailingState, SailingMove, Double> path) {
		return path.getHead().getRow() == SIZE -1 && path.getHead().getCol() == SIZE - 1 && path.getNumberOfNodes() < 2 * SIZE;
	}

	@Override
	public double getRequiredSuccessRate() {
		return .8;
	}

	@Override
	public int getAllowedTrainingIterations() {
		return 10000;
	}
}
