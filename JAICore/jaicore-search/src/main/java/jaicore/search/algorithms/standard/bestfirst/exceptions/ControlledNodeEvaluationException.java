package jaicore.search.algorithms.standard.bestfirst.exceptions;

/**
 * Use this exception if the node evaluation was rejected on purpose.
 * 
 * @author fmohr
 *
 */
@SuppressWarnings("serial")
public class ControlledNodeEvaluationException extends NodeEvaluationException {
	
	public ControlledNodeEvaluationException(String message) {
		super(message);
	}
}
