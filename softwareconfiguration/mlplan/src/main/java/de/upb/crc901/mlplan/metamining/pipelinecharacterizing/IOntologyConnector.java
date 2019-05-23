package de.upb.crc901.mlplan.metamining.pipelinecharacterizing;

import java.util.List;

/**
 * Encapsulates the connection to an ontology which holds knowledge about
 * classifiers, searchers, evaluators, and kernel function.
 * 
 * @author Helena Graf
 *
 */
public interface IOntologyConnector {

	/**
	 * Gets the ancestor concepts of this algorithm including the algorithms itself
	 * from most general to most specific concept. The algorithmName must be the
	 * name of an individual that is available in the ontology.
	 * 
	 * @param algorithmName
	 *            The algorithm for which to get the ancestors
	 * @return The ancestors of the algorithm in the ontology from farthest ancestor
	 *         to the algorithm itself
	 */
	public List<String> getAncestorsOfAlgorithm(String algorithmName);
}
