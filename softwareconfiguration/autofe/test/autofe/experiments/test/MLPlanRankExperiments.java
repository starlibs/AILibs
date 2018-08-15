package autofe.experiments.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.experiments.MLPlanRankExperimentEvaluator;
import jaicore.experiments.ExperimentRunner;

public class MLPlanRankExperiments {
	private static final Logger logger = LoggerFactory.getLogger(MLPlanRankExperiments.class);

	@Test
	public void executeMLPlanRankExperiments() {

		logger.info("Starting MLPLan data set generation and ranking experiments.");

		// Execute experiments
		ExperimentRunner runner = new ExperimentRunner(new MLPlanRankExperimentEvaluator());
		runner.randomlyConductExperiments(true);

		logger.info("Finished MLPLan data set generation and ranking experiments.");
	}
}
