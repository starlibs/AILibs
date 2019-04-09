package jaicore.experiments;

import java.io.File;

import org.aeonbits.owner.ConfigCache;

import com.fasterxml.jackson.databind.node.ObjectNode;

import jaicore.experiments.databasehandle.ExperimenterFileDBHandle;
import jaicore.experiments.exceptions.ExperimentEvaluationFailedException;

public class ExperimentRunnerTester implements IExperimentSetEvaluator {

	public class Generator implements IExperimentJSONKeyGenerator {

		@Override
		public int getNumberOfValues() {
			return 0;
		}

		@Override
		public ObjectNode getValue(final int i) {
			return null;
		}

	}


	public static void main(final String[] args) {
		IExperimentDatabaseHandle handle = new ExperimenterFileDBHandle(new File("testrsc/experiments.db"));
		IExperimentSetConfig config = ConfigCache.getOrCreate(IExperimentTesterConfig.class);
		System.out.println(config);
		IExperimentSetEvaluator evaluator = new ExperimentRunnerTester();

		ExperimentRunner runner = new ExperimentRunner(config, evaluator, handle);
		runner.randomlyConductExperiments(false);
	}


	@Override
	public void evaluate(final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor) throws ExperimentEvaluationFailedException {
		System.out.println("Running " + experimentEntry);
	}

}
