package ai.libs.jaicore.search.exampleproblems.enhancedttsp;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeGoalTester;

import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPState;

public class EnhancedTTSPSimpleSolutionPredicate implements NodeGoalTester<EnhancedTTSPState, String> {
	private EnhancedTTSP problem;

	public EnhancedTTSPSimpleSolutionPredicate(final EnhancedTTSP problem) {
		super();
		this.problem = problem;
	}

	@Override
	public boolean isGoal(final EnhancedTTSPState n) {
		return n.getCurTour().size() >= this.problem.getPossibleDestinations().size() && n.getCurLocation() == this.problem.getStartLocation();
	}

}
