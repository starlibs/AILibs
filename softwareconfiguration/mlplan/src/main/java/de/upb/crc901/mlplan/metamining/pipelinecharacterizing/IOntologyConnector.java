package de.upb.crc901.mlplan.metamining.pipelinecharacterizing;

import java.util.List;

/**
 * Encapsulates the connection to an ontology which holds knowledge about
 * classifiers, searchers, and evaluators.
 * 
 * @author Helena Graf
 *
 */
public interface IOntologyConnector {

	/**
	 * Gets the ancestor concepts of this classifier including the classifier itself
	 * from most general to most specific concept. The classifierName must be the
	 * name of an individual that is a classifier in the ontology.
	 * 
	 * @param classifierName
	 *            The classifier for which to get the ancestors
	 * @return The ancestors of the classifier from farthest to the classifier
	 *         itself
	 */
	public List<String> getAncestorsOfClassifier(String classifierName);

	/**
	 * Gets the ancestor concepts of this searcher algorithm (for attribute
	 * selection) including the searcher itself from most general to most specific
	 * concept. The searcherName must be the name of an individual that is an
	 * attribute selection algorithm in the ontology,
	 * 
	 * @param searcherName
	 *            The searcher for which to get the ancestors
	 * @return The ancestors of the classifier from farthest to the searcher itself
	 */
	public List<String> getAncestorsOfSearcher(String searcherName);

	/**
	 * Gets the ancestor concepts of this evaluator algorithm (for attribute
	 * selection) including the evaluator itself from most general to most specific
	 * concept. The evaluatorName must be the name of an individual that is a
	 * searcher algorithm in the ontology.
	 * 
	 * @param evaluatorName
	 *            The evaluator for which to get the ancestors
	 * @return The ancestors of the evaluator from farthest to the evaluator itself<
	 */
	public List<String> getAncestorsOfEvaluator(String evaluatorName);

}
