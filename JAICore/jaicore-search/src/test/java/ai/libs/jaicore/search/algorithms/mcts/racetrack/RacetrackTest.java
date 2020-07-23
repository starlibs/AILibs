package ai.libs.jaicore.search.algorithms.mcts.racetrack;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;

import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.search.algorithms.mcts.MCTSLearningSuccessTester;
import ai.libs.jaicore.search.exampleproblems.racetrack.RacetrackAction;
import ai.libs.jaicore.search.exampleproblems.racetrack.RacetrackReader;
import ai.libs.jaicore.search.exampleproblems.racetrack.RacetrackState;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class RacetrackTest extends MCTSLearningSuccessTester<RacetrackState, RacetrackAction> {

	@Override
	public IMDP<RacetrackState, RacetrackAction, Double> getMDP() {
		try {
			return new RacetrackReader().read(new File("testrsc/R-track.txt"), .9, new Random(0), true);
		} catch (IOException e) {
			LoggerUtil.logException(e);
			return null;
		}
	}

	@Override
	public boolean isSuccess(final IEvaluatedPath<RacetrackState, RacetrackAction, Double> path) {
		return path.getHead().isFinished();
	}

	@Override
	public double getRequiredSuccessRate() {
		return 1.0;
	}
}
