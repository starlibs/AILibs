package ai.libs.jaicore.search.testproblems.enhancedttsp.binarytelescope;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeGoalTester;

import ai.libs.jaicore.testproblems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.testproblems.enhancedttsp.EnhancedTTSPBinaryTelescopeNode;

public class EnhancedTTSPBinaryTelescopeSolutionPredicate implements NodeGoalTester<EnhancedTTSPBinaryTelescopeNode, String> {
	private EnhancedTTSP problem;

	public EnhancedTTSPBinaryTelescopeSolutionPredicate(final EnhancedTTSP problem) {
		super();
		this.problem = problem;
	}

	@Override
	public boolean isGoal(final EnhancedTTSPBinaryTelescopeNode n) {
		return n.getCurTour().size() >= this.problem.getPossibleDestinations().size() && n.getCurLocation() == this.problem.getStartLocation();
	}

}
