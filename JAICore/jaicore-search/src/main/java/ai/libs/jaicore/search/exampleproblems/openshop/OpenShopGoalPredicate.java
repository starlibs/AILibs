package ai.libs.jaicore.search.exampleproblems.openshop;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeGoalTester;

public class OpenShopGoalPredicate implements NodeGoalTester<OpenShopState, String> {

	@Override
	public boolean isGoal(final OpenShopState node) {
		return (node instanceof OpenShopOperationSelectionState) && ((OpenShopOperationSelectionState)node).getUnselectedOperations().isEmpty();
	}

}
