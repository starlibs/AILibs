package ai.libs.jaicore.search.algorithms.standard.bestfirst.exceptions;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;

/**
 * Use this exception if the node evaluation was rejected on purpose.
 * 
 * @author fmohr
 *
 */
@SuppressWarnings("serial")
public class ControlledNodeEvaluationException extends PathEvaluationException {
	
	public ControlledNodeEvaluationException(String message) {
		super(message);
	}
}
