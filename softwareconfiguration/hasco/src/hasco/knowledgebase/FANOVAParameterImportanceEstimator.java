package hasco.knowledgebase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Sets;

import hasco.core.HASCOWithParameterPruning;
import hasco.core.Util;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import jaicore.ml.core.FeatureSpace;
import jaicore.ml.intervaltree.ExtendedRandomForest;
import weka.core.Instances;

/**
 * Parameter importance estimator using fANOVA.
 * 
 * @author jmhansel
 *
 */
public class FANOVAParameterImportanceEstimator implements IParameterImportanceEstimator {
	private Map<String, ExtendedRandomForest> forests;
	// private Map<String, HashMap<ComponentInstance, Double>> performanceSamples;
	private PerformanceKnowledgeBase performanceKnowledgeBase;
	private String benchmarkName;
	private Map<String, HashMap<Set<Integer>, Double>> importanceDictionary;
	private Map<String, Set<String>> importantParameterMap;
	// private Map<String, HashMap<String, Double>>
	// importanceDictionaryForSingleComponents;

	public FANOVAParameterImportanceEstimator(PerformanceKnowledgeBase performanceKnowledgeBase, String benchmarkName) {
		forests = new HashMap<String, ExtendedRandomForest>();
		// this.performanceSamples = performanceSamples;
		this.performanceKnowledgeBase = performanceKnowledgeBase;
		this.benchmarkName = benchmarkName;
		this.importanceDictionary = new HashMap<String, HashMap<Set<Integer>, Double>>();
		this.importantParameterMap = new HashMap<String, Set<String>>();
		// this.importanceDictionaryForSingleComponents = new HashMap<String,
		// HashMap<String(), Double>>();
	}

	/**
	 * Initializes the random forests for the given performance benchmark.
	 * 
	 * @param benchmarkName
	 */
	// private void initializeForests(String benchmarkName) {
	// for (String identifier :
	// performanceKnowledgeBase.getPerformanceSamplesByIdentifier().get(benchmarkName)
	// .keySet()) {
	// if (forests.get(identifier) == null) {
	// ExtendedRandomForest curForest = new ExtendedRandomForest(5.0d, 16);
	// forests.put(identifier, curForest);
	// }
	// if (importanceDictionary.get(identifier) == null) {
	// HashMap<Set<Integer>, Double> importanceMap = new HashMap<Set<Integer>,
	// Double>();
	// importanceDictionary.put(identifier, importanceMap);
	// }
	// }
	// }

	/**
	 * Extract important parameters for subsets of size
	 * {@code sizeOfLargestSubsetToConsider}. Importance values are put into the
	 * importance dictionary. To recompute them, the flag {@code recompute} can be
	 * set.
	 * 
	 * @param composition
	 * @param importanceThreshold
	 * @param sizeOfLargestSubsetsToConsider
	 * @return
	 * @throws Exception
	 */
	public Set<String> extractImportantParameters(ComponentInstance composition, double importanceThreshold,
			int sizeOfLargestSubsetsToConsider, boolean recompute) throws Exception {
		Set<String> importantParameters = new HashSet<String>();
		String pipelineIdentifier = Util.getComponentNamesOfComposition(composition);
		if (importantParameterMap.containsKey(pipelineIdentifier))
			return importantParameterMap.get(pipelineIdentifier);
		// ExtendedRandomForest forest = forests.get(pipelineIdentifier);
		Instances data = performanceKnowledgeBase.createInstancesForPerformanceSamples(benchmarkName, composition);
		System.out.println("Extracting important parameters!");
		System.out.println(data);
		
		// if (forest == null) {
		// this.initializeForests(benchmarkName);
		// }
		// forest = forests.get(pipelineIdentifier);
		ExtendedRandomForest forest = new ExtendedRandomForest(1.0d, 32, new FeatureSpace(data));
		forest.buildClassifier(data);
		forest.prepareForest(data);
		if (!importanceDictionary.containsKey(pipelineIdentifier))
			importanceDictionary.put(pipelineIdentifier, new HashMap<Set<Integer>, Double>());
		double sum = 0;
		Set<Integer> parameterIndices = new HashSet<Integer>();
		for (int i = 0; i < data.numAttributes() - 1; i++)
			parameterIndices.add(i);
		// TODO initialize parameter indices
		for (int k = 1; k <= sizeOfLargestSubsetsToConsider; k++) {
			Set<Set<Integer>> currentSubsets = Sets.combinations(parameterIndices, k);
			// System.out.println("computing for parameter subsets: " + currentSubsets);
			for (Set<Integer> subset : currentSubsets) {
				double currentImportance = 1.0d;
				// if recomputation is desired of the dictionary has no importance value stored,
				// compute it
				if (recompute) {
					currentImportance = forest.computeMarginalVarianceContributionForFeatureSubset(subset);
					// sum += currentImportance;
					importanceDictionary.get(pipelineIdentifier).put(subset, currentImportance);
				} else if (importanceDictionary.get(pipelineIdentifier).containsKey(subset)) {
					System.out.println("Taking value from dictionary");
					currentImportance = importanceDictionary.get(pipelineIdentifier).get(subset);
				}
				// if no value is available in the dictionary, compute it
				else {
					currentImportance = forest.computeMarginalVarianceContributionForFeatureSubset(subset);
					importanceDictionary.get(pipelineIdentifier).put(subset, currentImportance);

				}
				System.out.println("Importance value for parameter subset " + subset + ": " + currentImportance);
				System.out.println("Importance value " + currentImportance + " >= " + importanceThreshold + ": "
						+ (currentImportance >= importanceThreshold));
				if (currentImportance >= importanceThreshold) {
					for (int i : subset) {
						importantParameters.add(forest.getFeatureSpace().getFeatureDomain(i).getName());
					}
				}
			}
		}
		// System.out.println("Importance overall: " + sum);
		importantParameterMap.put(pipelineIdentifier, importantParameters);
		int numPruned = data.numAttributes() -1 -importantParameters.size();
		HASCOWithParameterPruning.addPrunedParameters(numPruned);
		return importantParameters;

	}

	/**
	 * Extracts important parameters for all subset sizes.
	 * 
	 * @param composition
	 * @param importanceThreshold
	 * @return
	 * @throws Exception
	 */
	public Set<String> extractImportantParameters(ComponentInstance composition, double importanceThreshold,
			boolean recompute) throws Exception {
		String pipelineIdentifier = Util.getComponentNamesOfComposition(composition);
		Instances data = performanceKnowledgeBase.createInstancesForPerformanceSamples(benchmarkName, composition);
		// largest subset size = all attributes minus class attribute
		int k = data.numAttributes() - 1;
		return extractImportantParameters(composition, importanceThreshold, k, recompute);
	}

	public void extractImportanceForSingleComponents() {

	}
}
