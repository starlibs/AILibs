package ai.libs.jaicore.search.exampleproblems.enhancedttsp.binarytelescope;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;

import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPBinaryTelescopeNode;

public class EnhancedTTSPBinaryTelescopeSolutionPredicate implements INodeGoalTester<EnhancedTTSPBinaryTelescopeNode, String> {
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
