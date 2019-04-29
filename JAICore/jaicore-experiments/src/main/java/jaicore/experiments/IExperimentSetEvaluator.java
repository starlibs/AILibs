package jaicore.experiments;

import jaicore.experiments.exceptions.ExperimentEvaluationFailedException;

public interface IExperimentSetEvaluator {

	/**
	 * Method to compute a single point of the experiment set
	 *
	 * @param experimentEntry The point of the experiment set
	 * @param processor A handle to return intermediate results to the experiment runner routine
	 * @throws Exception
	 */
	public void evaluate(ExperimentDBEntry experimentEntry, IExperimentIntermediateResultProcessor processor) throws ExperimentEvaluationFailedException;
}
