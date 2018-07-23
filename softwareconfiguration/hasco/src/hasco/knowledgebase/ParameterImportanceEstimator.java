package hasco.knowledgebase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Sets;

import hasco.core.Util;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import jaicore.ml.intervaltree.ExtendedRandomForest;
import weka.core.Instances;

public class ParameterImportanceEstimator {
	private Map<String, ExtendedRandomForest> forests;
	// private Map<String, HashMap<ComponentInstance, Double>> performanceSamples;
	private PerformanceKnowledgeBase performanceKnowledgeBase;
	private String benchmarkName;
	private Map<String, HashMap<Set<Integer>, Double>> importanceDictionary;

	public ParameterImportanceEstimator(PerformanceKnowledgeBase performanceKnowledgeBase, String benchmarkName) {
		forests = new HashMap<String, ExtendedRandomForest>();
		// this.performanceSamples = performanceSamples;
		this.performanceKnowledgeBase = performanceKnowledgeBase;
		this.benchmarkName = benchmarkName;
		this.importanceDictionary = new HashMap<String, HashMap<Set<Integer>, Double>>();
	}

	/**
	 * Initializes the random forests for the given performance benchmark.
	 * 
	 * @param benchmarkName
	 */
	private void initializeForests(String benchmarkName) {
		for (String identifier : performanceKnowledgeBase.getPerformanceSamplesByIdentifier().get(benchmarkName)
				.keySet()) {
			if (forests.get(identifier) == null) {
				ExtendedRandomForest curForest = new ExtendedRandomForest();
				forests.put(identifier, curForest);
			}
			if(importanceDictionary.get(identifier) == null) {
				HashMap<Set<Integer>, Double> importanceMap = new HashMap<Set<Integer>,Double>();
				importanceDictionary.put(identifier, importanceMap);
			}
		}
	}
	

	public Set<String> extractImportantParameters(ComponentInstance composition, double importanceThreshold,
			int sizeOfLargestSubsetsToConsider) throws Exception {
		Set<String> importantParameters = new HashSet<String>();
		String pipelineIdentifier = Util.getComponentNamesOfComposition(composition);
		ExtendedRandomForest forest = forests.get(pipelineIdentifier);
		Instances data = performanceKnowledgeBase.createInstancesForPerformanceSamples(benchmarkName, composition);
		if (forest == null) {
			this.initializeForests(benchmarkName);
		}
		forest = forests.get(pipelineIdentifier);
		forest.buildClassifier(data);
		forest.prepareForest(data);
		double sum = 0;
		Set<Integer> parameterIndices = new HashSet<Integer>();
		for (int i = 0; i < data.numAttributes() - 1; i++)
			parameterIndices.add(i);
		// TODO initialize parameter indices
		// query importance values of extended random forest
		for (int k = 1; k <= sizeOfLargestSubsetsToConsider; k++) {
			Set<Set<Integer>> currentSubsets = Sets.combinations(parameterIndices, k);
			System.out.println("computing for parameter subsets: " + currentSubsets);
			for (Set<Integer> subset : currentSubsets) {
				double currentImportance = forest.computeMarginalForFeatureSubset(subset);
				sum += currentImportance;
				importanceDictionary.get(pipelineIdentifier).put(subset, currentImportance);
				if (currentImportance >= importanceThreshold) {
					for (int i : subset) {
						importantParameters.add(forest.getFeatureSpace().getFeatureDomain(i).getName());
					}
				}
			}
		}
		System.out.println("Importance overall: " + sum);
		return importantParameters;
	}

	public Set<String> extractImportantParameters(ComponentInstance composition, double importanceThreshold)
			throws Exception {
		String pipelineIdentifier = Util.getComponentNamesOfComposition(composition);
		Instances data = performanceKnowledgeBase.createInstancesForPerformanceSamples(benchmarkName, composition);
		// largest subset size = all attributes minus class attribute
		int k = data.numAttributes() - 1;
		return extractImportantParameters(composition, importanceThreshold, k);
	}
}
