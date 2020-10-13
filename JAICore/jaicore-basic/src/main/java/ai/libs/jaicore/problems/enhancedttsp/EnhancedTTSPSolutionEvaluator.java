package ai.libs.jaicore.problems.enhancedttsp;

import java.util.HashSet;
import java.util.Set;

import org.api4.java.common.attributedobjects.IObjectEvaluator;

import it.unimi.dsi.fastutil.shorts.ShortList;

public class EnhancedTTSPSolutionEvaluator implements IObjectEvaluator<ShortList, Double> {

	private final EnhancedTTSP problem;

	public EnhancedTTSPSolutionEvaluator(final EnhancedTTSP problem) {
		super();
		this.problem = problem;
	}

	@Override
	public Double evaluate(final ShortList solutionTour) throws InterruptedException {
		EnhancedTTSPState state = this.problem.getInitalState();
		Set<Short> seenLocations = new HashSet<>();
		for (short next : solutionTour) {
			try {
				if (seenLocations.contains(next)) {
					throw new IllegalArgumentException("Given tour is not a valid (partial) solution. Location " + next + " is contained at least twice!");
				}
				seenLocations.add(next);
				state = this.problem.computeSuccessorState(state, next);
			}
			catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Cannot evaluate tour " + solutionTour + " due to an error in successor computation of " + state + " with next step " + next + ". Message: " + e.getMessage());
			}
		}
		return state.getTime() - this.problem.getHourOfDeparture();
	}

}
