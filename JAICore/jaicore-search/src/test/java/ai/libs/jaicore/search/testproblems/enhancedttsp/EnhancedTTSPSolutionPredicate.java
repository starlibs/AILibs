package ai.libs.jaicore.search.testproblems.enhancedttsp;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;

import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPNode;

public class EnhancedTTSPSolutionPredicate implements INodeGoalTester<EnhancedTTSPNode, String> {
	private EnhancedTTSP problem;

	public EnhancedTTSPSolutionPredicate(final EnhancedTTSP problem) {
		super();
		this.problem = problem;
	}

	@Override
	public boolean isGoal(final EnhancedTTSPNode n) {
		return n.getCurTour().size() >= this.problem.getPossibleDestinations().size() && n.getCurLocation() == this.problem.getStartLocation();
	}

}
