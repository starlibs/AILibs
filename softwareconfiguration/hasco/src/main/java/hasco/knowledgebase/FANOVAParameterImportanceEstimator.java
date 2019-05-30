package hasco.knowledgebase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(FANOVAParameterImportanceEstimator.class);

	private PerformanceKnowledgeBase performanceKnowledgeBase;
	private String benchmarkName;
	private Map<String, HashMap<Set<Integer>, Double>> importanceDictionary;
	private Map<String, Set<String>> importantParameterMap;
	private int minNumSamples;
	private double importanceThreshold;
	private int sizeOfLargestSubsetToConsider;
	private Set<String> prunedParameters;

	public FANOVAParameterImportanceEstimator(final PerformanceKnowledgeBase performanceKnowledgeBase, final String benchmarkName, final int minNumSamples, final double importanceThreshold) {
		this.performanceKnowledgeBase = performanceKnowledgeBase;
		this.benchmarkName = benchmarkName;
		this.importanceDictionary = new HashMap<>();
		this.importantParameterMap = new HashMap<>();
		this.minNumSamples = minNumSamples;
		this.importanceThreshold = importanceThreshold;
		// For now only consider subsets of size <= 2
		this.sizeOfLargestSubsetToConsider = 2;
		this.prunedParameters = new HashSet<>();
	}

	public FANOVAParameterImportanceEstimator(final String benchmarkName, final int minNumSamples, final double importanceThreshold) {
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
	@Override
	public Set<String> extractImportantParameters(final ComponentInstance composition, final boolean recompute) throws ExtractionOfImportantParametersFailedException {
		String pipelineIdentifier = Util.getComponentNamesOfComposition(composition);
		if (this.importantParameterMap.containsKey(pipelineIdentifier)) {
			return this.importantParameterMap.get(pipelineIdentifier);
		}
		Instances data = this.performanceKnowledgeBase.getPerformanceSamples(this.benchmarkName, composition);
		FeatureSpace space = new FeatureSpace(data);
		Set<String> importantParameters = new HashSet<>();
		if (space.getDimensionality() < 2) {
			for (FeatureDomain domain : space.getFeatureDomains()) {
				importantParameters.add(domain.getName());
			}
			return importantParameters;
		}
		// Set of all parameters to compute difference later
		for (FeatureDomain domain : space.getFeatureDomains()) {
			this.prunedParameters.add(domain.getName());
		}
		ExtendedRandomForest forest = new ExtendedRandomForest();
		// forest.setMinNumSamples
		try {
			forest.buildClassifier(data);
			forest.prepareForest(data);
		} catch (Exception e) {
			throw new ExtractionOfImportantParametersFailedException("Could not build model", e);
		}
		if (!this.importanceDictionary.containsKey(pipelineIdentifier)) {
			this.importanceDictionary.put(pipelineIdentifier, new HashMap<Set<Integer>, Double>());
		}
		Set<Integer> parameterIndices = new HashSet<>();
		for (int i = 0; i < data.numAttributes() - 1; i++) {
			parameterIndices.add(i);
		}
		// for now we only consider subsets of size k <= 2
		for (int k = 1; k <= this.sizeOfLargestSubsetToConsider; k++) {
			Set<Set<Integer>> currentSubsets = Sets.combinations(parameterIndices, k);
			for (Set<Integer> subset : currentSubsets) {
				double currentImportance;
				// if recomputation is desired of the dictionary has no importance value stored, compute it
				if (recompute) {
					currentImportance = forest.computeMarginalVarianceContributionForFeatureSubset(subset);
					this.importanceDictionary.get(pipelineIdentifier).put(subset, currentImportance);
				} else if (this.importanceDictionary.get(pipelineIdentifier).containsKey(subset)) {
					LOGGER.debug("Taking value from dictionary");
					currentImportance = this.importanceDictionary.get(pipelineIdentifier).get(subset);
				}
				// if no value is available in the dictionary, compute it
				else {
					currentImportance = forest.computeMarginalVarianceContributionForFeatureSubset(subset);
					this.importanceDictionary.get(pipelineIdentifier).put(subset, currentImportance);
					if (Double.isNaN(currentImportance)) {
						currentImportance = 1.0;
						LOGGER.debug("importance value is NaN, so it will be set to 1");
					}
				}
				LOGGER.debug("Importance value for parameter subset {}: {}", subset, currentImportance);
				LOGGER.debug("Importance value {} >= {}" + ": ", currentImportance, this.importanceThreshold, (currentImportance >= this.importanceThreshold));
				if (currentImportance >= this.importanceThreshold) {
					for (int i : subset) {
						importantParameters.add(forest.getFeatureSpace().getFeatureDomain(i).getName());
					}
				}
			}
		}
		this.importantParameterMap.put(pipelineIdentifier, importantParameters);
		this.prunedParameters.removeAll(importantParameters);
		return importantParameters;

	}

	/**
	 * Computes importance values for individual components.
	 */
	@Override
	public Map<String, Double> computeImportanceForSingleComponent(final Component component) {
		Instances data = this.performanceKnowledgeBase.getPerformanceSamplesForIndividualComponent(this.benchmarkName, component);
		if (data == null) {
			return null;
		}
		ExtendedRandomForest forest = new ExtendedRandomForest();
		HashMap<String, Double> result = new HashMap<>();
		try {
			forest.buildClassifier(data);
			for (int i = 0; i < data.numAttributes() - 1; i++) {
				HashSet<Integer> set = new HashSet<>();
				set.add(i);
				double importance = forest.computeMarginalVarianceContributionForFeatureSubset(set);
				result.put(data.attribute(i).name(), importance);
			}
		} catch (Exception e) {
			LOGGER.error("Could not build model and compute marginal variance contribution.", e);
		}
		return result;
	}

	@Override
	public boolean readyToEstimateImportance(final ComponentInstance composition) {
		return this.performanceKnowledgeBase.kDistinctAttributeValuesAvailable(this.benchmarkName, composition, this.minNumSamples);
	}

	/**
	 * @return the performanceKnowledgeBase
	 */
	@Override
	public PerformanceKnowledgeBase getPerformanceKnowledgeBase() {
		return this.performanceKnowledgeBase;
	}

	/**
	 * @param performanceKnowledgeBase the performanceKnowledgeBase to set
	 */
	@Override
	public void setPerformanceKnowledgeBase(final PerformanceKnowledgeBase performanceKnowledgeBase) {
		this.performanceKnowledgeBase = performanceKnowledgeBase;
	}

	@Override
	public int getNumberPrunedParameters() {
		return this.prunedParameters.size();
	}

	@Override
	public Set<String> getPrunedParameters() {
		return this.prunedParameters;
	}
}
