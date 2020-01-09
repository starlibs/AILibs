package ai.libs.jaicore.experiments.mlexample;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.db.IDatabaseConfig;
import ai.libs.jaicore.experiments.ExperimentDBEntry;
import ai.libs.jaicore.experiments.ExperimentDatabasePreparer;
import ai.libs.jaicore.experiments.ExperimentRunner;
import ai.libs.jaicore.experiments.IExperimentDatabaseHandle;
import ai.libs.jaicore.experiments.IExperimentIntermediateResultProcessor;
import ai.libs.jaicore.experiments.IExperimentSetEvaluator;
import ai.libs.jaicore.experiments.databasehandle.ExperimenterMySQLHandle;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyExistsInDatabaseException;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.IllegalExperimentSetupException;

public class MachineLearningExperimenter {

	/**
	 * Variables for the experiment and database setup
	 */
	private static final File configFile = new File("testrsc/mlexample/setup.properties");
	private static final IExampleMCCConfig m = (IExampleMCCConfig)ConfigCache.getOrCreate(IExampleMCCConfig.class).loadPropertiesFromFile(configFile);
	private static final IDatabaseConfig dbconfig = (IDatabaseConfig)ConfigFactory.create(IDatabaseConfig.class).loadPropertiesFromFile(configFile);
	private static final IExperimentDatabaseHandle dbHandle = new ExperimenterMySQLHandle(dbconfig);
	private static final Logger logger = LoggerFactory.getLogger(MachineLearningExperimenter.class);

	public static void main(final String[] args) throws ExperimentDBInteractionFailedException, AlgorithmTimeoutedException, IllegalExperimentSetupException, ExperimentAlreadyExistsInDatabaseException, InterruptedException, AlgorithmExecutionCanceledException {
		runExperiments();
	}

	public static void createTableWithExperiments() throws ExperimentDBInteractionFailedException, AlgorithmTimeoutedException, IllegalExperimentSetupException, ExperimentAlreadyExistsInDatabaseException, InterruptedException, AlgorithmExecutionCanceledException {
		ExperimentDatabasePreparer preparer = new ExperimentDatabasePreparer(m, dbHandle);
		preparer.synchronizeExperiments();
	}

	public static void deleteTable() throws ExperimentDBInteractionFailedException {
		dbHandle.deleteDatabase();
	}

	public static void runExperiments() throws ExperimentDBInteractionFailedException, InterruptedException {
		Random r = new Random(System.currentTimeMillis());
		ExperimentRunner runner = new ExperimentRunner(m, new IExperimentSetEvaluator() {

			@Override
			public void evaluate(final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor) throws InterruptedException {

				/* get experiment setup */
				Map<String, String> description = experimentEntry.getExperiment().getValuesOfKeyFields();
				String classifierName = description.get("classifier");
				String datasetName = description.get("dataset");
				int seed = Integer.parseInt(description.get("seed"));

				/* create objects for experiment */
				logger.info("Evaluate {} for dataset {} and seed {}", classifierName, datasetName, seed);

				/* run fictive experiment */
				Map<String, Object> results = new HashMap<>();
				long timeStartTraining = System.currentTimeMillis();
				Thread.sleep(r.nextInt(10));
				results.put("traintime", System.currentTimeMillis() - timeStartTraining);

				/* report results */
				double loss = r.nextDouble();
				results.put("loss", loss);
				processor.processResults(results);
			}
		}, dbHandle);
		runner.randomlyConductExperiments(10);
	}
}
