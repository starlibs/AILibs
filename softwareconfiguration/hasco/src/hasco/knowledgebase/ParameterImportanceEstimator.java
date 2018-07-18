package hasco.knowledgebase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import hasco.core.Util;
import hasco.model.ComponentInstance;
import jaicore.ml.intervaltree.ExtendedRandomForest;
import weka.core.Instances;

public class ParameterImportanceEstimator {
	private Map<String, ExtendedRandomForest> forests;
	private Map<String, HashMap<ComponentInstance, Double>> performanceSamples;
	private PerformanceKnowledgeBase performanceKnowledgeBase;
	private String benchmarkName;

	public ParameterImportanceEstimator(PerformanceKnowledgeBase performanceKnowledgeBase, String benchmarkName) {
		forests = new HashMap<String, ExtendedRandomForest>();
		this.performanceSamples = performanceSamples;
		this.performanceKnowledgeBase = performanceKnowledgeBase;
		this.benchmarkName = benchmarkName;
	}

	/**
	 * Initializes the random forests for the given performance benchmark.
	 * 
	 * @param benchmarkName
	 */
	private void initiliazeForests(String benchmarkName) {
		for (HashMap<ComponentInstance, Double> samples : performanceSamples.values()) {
			for (ComponentInstance componentInstance : samples.keySet()) {
				if (forests.get(componentInstance) == null) {
					ExtendedRandomForest curForest = new ExtendedRandomForest();
					forests.put(Util.getComponentNamesOfComposition(componentInstance), curForest);
				}
			}
		}
	}

	public Set<Integer> extractImportanceParameters(ComponentInstance composition, double importanceThreshold,
			int sizeOfLargestSubsetsToConsider) throws Exception {
		Set<Integer> importantParameters = new HashSet<Integer>();
		String pipelineIdentifier = Util.getComponentNamesOfComposition(composition);
		ExtendedRandomForest forest = forests.get(pipelineIdentifier);
		Instances data = performanceKnowledgeBase.createInstancesForPerformanceSamples(benchmarkName, composition);
		forest.buildClassifier(data);
		forest.prepareForest();
		Set<Integer> parameterIndices = new HashSet<Integer>();
		for (int i = 0; i < data.numAttributes(); i++)
			parameterIndices.add(i);
		// TODO initialize parameter indices
		// query importance values of extended random forest
		for (int k = 1; k <= sizeOfLargestSubsetsToConsider; k++) {
			Set<Set<Integer>> currentSubsets = Sets.combinations(parameterIndices, k);
			for (Set<Integer> subset : currentSubsets) {
				double currentImportance = forest.computeMarginalForFeatureSubset(subset);
				if (currentImportance >= importanceThreshold)
					importantParameters.addAll(subset);
			}
		}
		return importantParameters;
	}
}
