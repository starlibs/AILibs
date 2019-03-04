package jaicore.search.testproblems.knapsack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import jaicore.basic.algorithm.AlgorithmTestProblemSetForSolutionIterators;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.testproblems.knapsack.KnapsackConfiguration;
import jaicore.testproblems.knapsack.KnapsackProblem;
import jaicore.testproblems.knapsack.KnapsackProblemSet;

public class KnapsackProblemAsGraphSearchSet extends AlgorithmTestProblemSetForSolutionIterators<GraphSearchInput<KnapsackConfiguration, String>, List<String>> {
	private final KnapsackProblemSet problemSet = new KnapsackProblemSet();
	private final KnapsackToGraphSearchProblemInputReducer reducer = new KnapsackToGraphSearchProblemInputReducer();
	
	public KnapsackProblemAsGraphSearchSet() {
		super("Knapsack problem as graph search");
	}

	@Override
	public Map<GraphSearchInput<KnapsackConfiguration, String>, Collection<List<String>>> getProblemsWithSolutions() {
		Map<GraphSearchInput<KnapsackConfiguration, String>, Collection<List<String>>> problems = new HashMap<>();
		for (Entry<KnapsackProblem, Collection<Set<String>>> problemWithSolutions : problemSet.getProblemsWithSolutions().entrySet()) {
			GraphSearchInput<KnapsackConfiguration, String> transformedInput = reducer.transform(problemWithSolutions.getKey());
			Collection<Set<String>> items = problemWithSolutions.getValue();
			Collection<List<String>> sortedItems = new ArrayList<>();
			items.forEach(s -> sortedItems.add(s.stream().sorted().collect(Collectors.toList())));
			problems.put(transformedInput, sortedItems);
		}
		return problems;
	}

	@Override
	public GraphSearchInput<KnapsackConfiguration, String> getSimpleProblemInputForGeneralTestPurposes() throws Exception {
		return reducer.transform(problemSet.getSimpleProblemInputForGeneralTestPurposes());
	}

	@Override
	public GraphSearchInput<KnapsackConfiguration, String> getDifficultProblemInputForGeneralTestPurposes() throws Exception {
		return reducer.transform(problemSet.getDifficultProblemInputForGeneralTestPurposes());
	}
}
