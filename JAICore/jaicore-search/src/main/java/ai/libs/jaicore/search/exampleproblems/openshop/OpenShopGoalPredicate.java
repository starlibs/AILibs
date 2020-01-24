package ai.libs.jaicore.search.exampleproblems.openshop;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;

public class OpenShopGoalPredicate implements INodeGoalTester<OpenShopState, String> {

	@Override
	public boolean isGoal(final OpenShopState node) {
		return (node instanceof OpenShopOperationSelectionState) && ((OpenShopOperationSelectionState)node).getUnselectedOperations().isEmpty();
	}

}
