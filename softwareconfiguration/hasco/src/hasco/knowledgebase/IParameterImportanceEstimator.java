package hasco.knowledgebase;

import java.util.Map;
import java.util.Set;

import hasco.model.Component;
import hasco.model.ComponentInstance;

public interface IParameterImportanceEstimator {
	
	/**
	 * Extracts the parameters of a composition that reach the given threshold w.r.t. importance
	 * @param composition
	 * @param importanceThreshold
	 * @param sizeOfLargestSubsetsToConsider
	 * @param recompute
	 * @return
	 * @throws Exception
	 */
	public Set<String> extractImportantParameters(ComponentInstance composition, double importanceThreshold,
			int sizeOfLargestSubsetsToConsider, boolean recompute) throws Exception;
	
	/**
	 * Computes importance values for an individual component
	 * @param component
	 * @return
	 */
	public Map<String, Double> computeImportanceForSingleComponent(Component component);

}
