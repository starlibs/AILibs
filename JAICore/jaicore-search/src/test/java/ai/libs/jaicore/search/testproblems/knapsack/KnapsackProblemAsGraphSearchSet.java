package ai.libs.jaicore.search.testproblems.knapsack;

import java.util.Set;

import ai.libs.jaicore.basic.algorithm.ReductionBasedAlgorithmTestProblemSet;
import ai.libs.jaicore.problems.knapsack.KnapsackConfiguration;
import ai.libs.jaicore.problems.knapsack.KnapsackProblem;
import ai.libs.jaicore.problemsets.knapsack.KnapsackProblemSet;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class KnapsackProblemAsGraphSearchSet extends ReductionBasedAlgorithmTestProblemSet<GraphSearchWithSubpathEvaluationsInput<KnapsackConfiguration, String, Double>, SearchGraphPath<KnapsackConfiguration, String>, KnapsackProblem, Set<String>> {

	public KnapsackProblemAsGraphSearchSet() {
		super("Knapsack problem as graph search", new KnapsackProblemSet(), new KnapsackToGraphSearchReducer());
	}
}
