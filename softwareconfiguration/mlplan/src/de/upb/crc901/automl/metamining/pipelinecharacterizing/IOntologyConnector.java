package de.upb.crc901.automl.metamining.pipelinecharacterizing;

import java.util.List;

public interface IOntologyConnector {
	/**
	 * Gets the parent concepts of this classifier including the classifier itself
	 * from most general to most specific concept.
	 * 
	 * @param classifierName
	 * @return
	 */
	public List<String> getParentsOfClassifier(String classifierName);
	
	public List<String> getParentsOfSearcher(String searcher);
	
	public List<String> getParentsOfEvaluator(String evaluator);

}
