package hasco.knowledgebase;

import java.util.Set;

import hasco.model.ComponentInstance;

public interface IParameterImportanceEstimator {
	
	public Set<String> extractImportantParameters(ComponentInstance composition, double importanceThreshold,
			int sizeOfLargestSubsetsToConsider, boolean recompute) throws Exception;

}
