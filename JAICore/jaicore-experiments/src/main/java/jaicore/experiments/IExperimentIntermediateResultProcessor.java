package jaicore.experiments;

import java.util.Map;

/**
 * A result processor is used to push new result values to the database when they arrive.
 * The result processor internally knows to which experiment the pushed values belong.
 * 
 * @author fmohr
 *
 */
public interface IExperimentIntermediateResultProcessor {
	
	/**
	 * The result fields and the values that should be pushed for them.
	 * @param results
	 */
	public void processResults(Map<String,Object> results);
}
