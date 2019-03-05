package jaicore.search.testproblems.enhancedttsp;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jaicore.basic.algorithm.IAlgorithmTestProblemSetForSolutionIterators;
import jaicore.search.probleminputs.GraphSearchInput;

public class EnhancedTTSPAsGraphSearchSet extends IAlgorithmTestProblemSetForSolutionIterators<GraphSearchInput<EnhancedTTSPNode, String>, List<Short>> {

	private final EnhancedTTSPProblemSet problemSet = new EnhancedTTSPProblemSet();
	private final EnhancedTTSPToGraphSearchProblemInputReducer reducer = new EnhancedTTSPToGraphSearchProblemInputReducer();
	
	public EnhancedTTSPAsGraphSearchSet() {
		super("Enhanced TTSP as graph search");
	}

	@Override
	public Map<GraphSearchInput<EnhancedTTSPNode, String>, Collection<List<Short>>> getProblemsWithSolutions() {
		Map<GraphSearchInput<EnhancedTTSPNode, String>, Collection<List<Short>>> problems = new HashMap<>();
		for (Entry<EnhancedTTSP, Collection<List<Short>>> problemWithSolutions : problemSet.getProblemsWithSolutions().entrySet()) {
			GraphSearchInput<EnhancedTTSPNode, String> transformedInput = reducer.transform(problemWithSolutions.getKey());
			problems.put(transformedInput, problemWithSolutions.getValue());
		}
		return problems;
	}

	@Override
	public GraphSearchInput<EnhancedTTSPNode, String> getSimpleProblemInputForGeneralTestPurposes() throws Exception {
		return reducer.transform(problemSet.getSimpleProblemInputForGeneralTestPurposes());
	}

	@Override
	public GraphSearchInput<EnhancedTTSPNode, String> getDifficultProblemInputForGeneralTestPurposes() throws Exception {
		return reducer.transform(problemSet.getDifficultProblemInputForGeneralTestPurposes());
	}

}
