package ai.libs.jaicore.problems.enhancedttsp;

import org.api4.java.common.attributedobjects.IObjectEvaluator;

import it.unimi.dsi.fastutil.shorts.ShortList;

public class EnhancedTTSPSolutionEvaluator implements IObjectEvaluator<ShortList, Double> {

	private final EnhancedTTSP problem;

	public EnhancedTTSPSolutionEvaluator(final EnhancedTTSP problem) {
		super();
		this.problem = problem;
	}

	@Override
	public Double evaluate(final ShortList solutionTour) {
		EnhancedTTSPState state = this.problem.getInitalState();
		for (short next : solutionTour) {
			try {
				state = this.problem.computeSuccessorState(state, next);
			}
			catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Cannot evaluate tour " + solutionTour + " due to an error in successor computation of " + state + " with next step " + next + ". Message: " + e.getMessage());
			}
		}
		return state.getTime() - this.problem.getHourOfDeparture();
	}

}
