package jaicore.testproblems.enhancedttsp;

import it.unimi.dsi.fastutil.shorts.ShortList;
import jaicore.basic.IObjectEvaluator;

public class EnhancedTTSPSolutionEvaluator implements IObjectEvaluator<ShortList, Double> {

	private final EnhancedTTSP problem;

	public EnhancedTTSPSolutionEvaluator(final EnhancedTTSP problem) {
		super();
		this.problem = problem;
	}

	@Override
	public Double evaluate(final ShortList solutionTour) {
		EnhancedTTSPNode state = this.problem.getInitalState();
		for (short next : solutionTour) {
			state = this.problem.computeSuccessorState(state, next);
		}
		return state.getTime() - this.problem.getHourOfDeparture();
	}

}
