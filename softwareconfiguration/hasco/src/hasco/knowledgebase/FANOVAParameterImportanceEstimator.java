package hasco.knowledgebase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import jaicore.ml.core.FeatureDomain;
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
	// private Map<String, HashMap<ComponentInstance, Double>> performanceSamples;
	private PerformanceKnowledgeBase performanceKnowledgeBase;
	private String benchmarkName;
	private Map<String, HashMap<Set<Integer>, Double>> importanceDictionary;
	private Map<String, Set<String>> importantParameterMap;
	private int minNumSamples;
	private double importanceThreshold;
	private int sizeOfLargestSubsetToConsider;
	// private Map<String, HashMap<String, Double>>
	// importanceDictionaryForSingleComponents;

	public FANOVAParameterImportanceEstimator(PerformanceKnowledgeBase performanceKnowledgeBase, String benchmarkName,
			int minNumSamples, double importanceThreshold) {
		// this.performanceSamples = performanceSamples;
		this.performanceKnowledgeBase = performanceKnowledgeBase;
		this.benchmarkName = benchmarkName;
		this.importanceDictionary = new HashMap<String, HashMap<Set<Integer>, Double>>();
		this.importantParameterMap = new HashMap<String, Set<String>>();
		// this.importanceDictionaryForSingleComponents = new HashMap<String,
		// HashMap<String(), Double>>();
		this.minNumSamples = minNumSamples;
		this.importanceThreshold = importanceThreshold;
		// For now only consider subsets of size <= 2
		this.sizeOfLargestSubsetToConsider = 2;
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

	public FANOVAParameterImportanceEstimator(String benchmarkName, int minNumSamples, double importanceThreshold) {
		this(null, benchmarkName, minNumSamples, importanceThreshold);
	}

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
	public Set<String> extractImportantParameters(ComponentInstance composition, boolean recompute) throws Exception {
		Set<String> importantParameters = new HashSet<String>();
		String pipelineIdentifier = Util.getComponentNamesOfComposition(composition);
		if (importantParameterMap.containsKey(pipelineIdentifier))
			return importantParameterMap.get(pipelineIdentifier);
		// ExtendedRandomForest forest = forests.get(pipelineIdentifier);
		Instances data = performanceKnowledgeBase.createInstancesForPerformanceSamples(benchmarkName, composition);
		// if (forest == null) {
		// this.initializeForests(benchmarkName);
		// }
		// forest = forests.get(pipelineIdentifier);
		FeatureSpace space = new FeatureSpace(data);
		if(space.getDimensionality()<2) {
			for(FeatureDomain domain : space.getFeatureDomains()) {
				importantParameters.add(domain.getName());
			}
			return importantParameters;
		}
		ExtendedRandomForest forest = new ExtendedRandomForest();
		// forest.setMinNumSamples
		// TODO setter for forest
		System.out.println("data for forest: " + data);
		forest.buildClassifier(data);
		forest.prepareForest(data);
		if (!importanceDictionary.containsKey(pipelineIdentifier))
			importanceDictionary.put(pipelineIdentifier, new HashMap<Set<Integer>, Double>());
		double sum = 0;
		Set<Integer> parameterIndices = new HashSet<Integer>();
		for (int i = 0; i < data.numAttributes() - 1; i++)
			parameterIndices.add(i);
		// for now we only consider subsets of size k <= 2
		for (int k = 1; k <= this.sizeOfLargestSubsetToConsider; k++) {
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
					if (Double.isNaN(currentImportance)) {
						currentImportance = 1.0;
						System.out.println("importance value is NaN, so it will be set to 1");
					}
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
		int numPruned = data.numAttributes() - 1 - importantParameters.size();
		return importantParameters;

	}

	/**
	 * Computes importance values for individual components.
	 */
	public Map<String, Double> computeImportanceForSingleComponent(Component component) {
		HashMap<String, Double> result = new HashMap<String, Double>();
		Instances data = performanceKnowledgeBase.getPerformanceSamplesForIndividualComponent(benchmarkName, component);
		System.out.println(data);
		if (data == null)
			return null;
		// ExtendedRandomForest forest = new ExtendedRandomForest(1.0d, 32, new
		// FeatureSpace(data));
		FeatureSpace space = new FeatureSpace(data);
		ExtendedRandomForest forest = new ExtendedRandomForest();
		// TODO setter for forest
		try {
			forest.buildClassifier(data);
			forest.prepareForest(data);
			for (int i = 0; i < data.numAttributes() - 1; i++) {
				HashSet<Integer> set = new HashSet<Integer>();
				set.add(i);
				double importance = forest.computeMarginalVarianceContributionForFeatureSubset(set);
				result.put(data.attribute(i).name(), importance);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public boolean readyToEstimateImportance(ComponentInstance composition) {
		return this.performanceKnowledgeBase.kDistinctAttributeValuesAvailable(benchmarkName, composition,
				minNumSamples);
	}

	/**
	 * @return the performanceKnowledgeBase
	 */
	public PerformanceKnowledgeBase getPerformanceKnowledgeBase() {
		return performanceKnowledgeBase;
	}

	/**
	 * @param performanceKnowledgeBase the performanceKnowledgeBase to set
	 */
	public void setPerformanceKnowledgeBase(PerformanceKnowledgeBase performanceKnowledgeBase) {
		this.performanceKnowledgeBase = performanceKnowledgeBase;
	}
}
