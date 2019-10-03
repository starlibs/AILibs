package ai.libs.jaicore.problems.enhancedttsp;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
		if (solutionTour.size() != this.problem.getLocations().size()) {
			throw new IllegalArgumentException("Given tour is not a solution, because " + solutionTour.size() + " locations are visited while any solution tour must visit exactly " + this.problem.getLocations().size() + " locations. Locations contained in tour: " + solutionTour.stream().map(d -> "\n\t" + d).collect(Collectors.joining()) + "\nLocations: " + this.problem.getLocations().stream().map(d -> "\n\t" + d).collect(Collectors.joining()));
		}
		if (solutionTour.getShort(solutionTour.size() - 1) != this.problem.getStartLocation()) {
			throw new IllegalArgumentException("Given solution tour does terminate in " + solutionTour.getShort(solutionTour.size() - 1) + ", which is not the start location " + this.problem.getStartLocation() + "!");
		}
		Set<Short> seenLocations = new HashSet<>();
		for (short next : solutionTour) {
			try {
				if (seenLocations.contains(next)) {
					throw new IllegalArgumentException("Given tour is not a solution. Location " + next + " is contained at least twice!");
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
