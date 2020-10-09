package ai.libs.jaicore.search.algorithms.mcts.lake;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;

import ai.libs.jaicore.search.algorithms.mcts.MCTSLearningSuccessTester;
import ai.libs.jaicore.search.exampleproblems.lake.ELakeActions;
import ai.libs.jaicore.search.exampleproblems.lake.JasminLakeMDP;
import ai.libs.jaicore.search.exampleproblems.lake.LakeMDP;
import ai.libs.jaicore.search.exampleproblems.lake.TimedLakeState;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class LakeTest extends MCTSLearningSuccessTester<TimedLakeState, ELakeActions> {

	private LakeMDP lastMDP;

	@Override
	public void preMCTSHook() {
		this.logger.info("The lake looks as follows:\n{}", this.lastMDP.getInitState().getStringVisualization());
	}

	@Override
	public IMDP<TimedLakeState, ELakeActions, Double> getMDP() {
		LakeMDP mdp = new JasminLakeMDP(0);
		this.lastMDP = mdp;
		return mdp;
	}

	@Override
	public boolean isSuccess(final IEvaluatedPath<TimedLakeState, ELakeActions, Double> path) {
		return this.lastMDP.isGoalState(path.getHead());
	}

	@Override
	public double getRequiredSuccessRate() {
		return .7;
	}
}
