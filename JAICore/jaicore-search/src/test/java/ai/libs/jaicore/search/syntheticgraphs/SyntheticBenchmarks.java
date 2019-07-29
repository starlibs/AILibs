package ai.libs.jaicore.search.syntheticgraphs;

import java.io.File;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.experiments.ExperimentDatabasePreparer;
import ai.libs.jaicore.experiments.ExperimentRunner;
import ai.libs.jaicore.experiments.IExperimentDatabaseHandle;
import ai.libs.jaicore.experiments.databasehandle.ExperimenterMySQLHandle;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyExistsInDatabaseException;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.IllegalExperimentSetupException;

public class SyntheticBenchmarks {
	private static ISyntheticSearchExperimentConfig config;
	private static IExperimentDatabaseHandle databaseHandle;

	public static void main(final String[] args)
			throws AlgorithmTimeoutedException, ExperimentDBInteractionFailedException, IllegalExperimentSetupException, ExperimentAlreadyExistsInDatabaseException, InterruptedException, AlgorithmExecutionCanceledException {
		File coreFile = new File("conf/synthetic-experiments.conf");
		File databaseFile = new File("conf/experiments-database.conf");

		config = (ISyntheticSearchExperimentConfig) ConfigFactory.create(ISyntheticSearchExperimentConfig.class).loadPropertiesFromFile(coreFile).loadPropertiesFromFile(databaseFile);
		databaseHandle = new ExperimenterMySQLHandle(config);

		//		setup();
		new ExperimentRunner(config, new SearchBenchmarker(), databaseHandle).randomlyConductExperiments();
	}

	public static void setup()
			throws ExperimentDBInteractionFailedException, AlgorithmTimeoutedException, IllegalExperimentSetupException, ExperimentAlreadyExistsInDatabaseException, InterruptedException, AlgorithmExecutionCanceledException {
		//		databaseHandle.deleteDatabase();
		ExperimentDatabasePreparer preparer = new ExperimentDatabasePreparer(config, databaseHandle);
		preparer.synchronizeExperiments();
	}
}
