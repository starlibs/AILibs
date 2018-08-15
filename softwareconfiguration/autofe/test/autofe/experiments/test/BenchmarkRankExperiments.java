package autofe.experiments.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.experiments.BenchmarkRankExperimentEvaluator;
import jaicore.experiments.ExperimentRunner;

public class BenchmarkRankExperiments {
	private static final Logger logger = LoggerFactory.getLogger(BenchmarkRankExperiments.class);

	@Test
	public void executeMLPlanRankExperiments() {

		logger.info("Starting benchmark ranking experiments.");

		// Execute experiments
		ExperimentRunner runner = new ExperimentRunner(new BenchmarkRankExperimentEvaluator());
		runner.randomlyConductExperiments(true);

		logger.info("Finished benchmark ranking experiments.");
	}
}
