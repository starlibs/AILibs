package hasco.knowledgebase;

import java.util.Map;
import java.util.Set;

import hasco.model.Component;
import hasco.model.ComponentInstance;

public interface IParameterImportanceEstimator {

	/**
	 * Extracts the parameters of a composition that reach the given threshold
	 * w.r.t. importance
	 *
	 * @param composition
	 * @param importanceThreshold
	 * @param sizeOfLargestSubsetsToConsider
	 * @param recompute
	 * @return
	 * @throws Exception
	 */
	public Set<String> extractImportantParameters(ComponentInstance composition, boolean recompute) throws ExtractionOfImportantParametersFailedException;

	/**
	 * Computes importance values for an individual component
	 *
	 * @param component
	 * @return
	 */
	public Map<String, Double> computeImportanceForSingleComponent(Component component);

	/**
	 * Checks whether the estimator is ready to estimate parameter importance for
	 * the composition
	 *
	 * @param composition
	 * @return true if the estimator is ready, false otherwise
	 */
	public boolean readyToEstimateImportance(ComponentInstance composition);

	/**
	 * Set the performance knowledge base used for parameter importance estimation
	 * @param performanceKB
	 */
	public void setPerformanceKnowledgeBase(PerformanceKnowledgeBase performanceKB);

	/**
	 * Get the performance knowledge base used for parameter importance estimation
	 */
	public PerformanceKnowledgeBase getPerformanceKnowledgeBase();

	/**
	 * Returns the number of parameters that have been pruned
	 * @return number of pruned parameters
	 */
	public int getNumberPrunedParameters();

	public Set<String> getPrunedParameters();

}
