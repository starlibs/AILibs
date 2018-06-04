package jaicore.experiments;

import jaicore.basic.SQLAdapter;

public interface IExperimentSetEvaluator {

	/**
	 * @return the experiment set configuration that describes the experiment set, which consists of the description of the resources, the database connection, independent and dependent variables
	 */
	public IExperimentSetConfig getConfig();

	/**
	 * Method to compute a single point of the experiment set
	 * 
	 * @param experimentEntry The point of the experiment set
	 * @param adapter The sql adapter for the case that the evaluator wants to store more information in the database
	 * @param processor A handle to return intermediate results to the experiment runner routine
	 * @throws Exception
	 */
	public void evaluate(ExperimentDBEntry experimentEntry, SQLAdapter adapter, IExperimentIntermediateResultProcessor processor) throws Exception;
}
