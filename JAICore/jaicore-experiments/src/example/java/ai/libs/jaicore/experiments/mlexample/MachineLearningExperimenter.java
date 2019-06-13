package ai.libs.jaicore.experiments.mlexample;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.ConfigFactory;

import ai.libs.jaicore.experiments.ExperimentDBEntry;
import ai.libs.jaicore.experiments.ExperimentRunner;
import ai.libs.jaicore.experiments.IDatabaseConfig;
import ai.libs.jaicore.experiments.IExperimentIntermediateResultProcessor;
import ai.libs.jaicore.experiments.IExperimentSetEvaluator;
import ai.libs.jaicore.experiments.databasehandle.ExperimenterSQLHandle;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.IllegalExperimentSetupException;

public class MachineLearningExperimenter {

	public static void main(final String[] args) throws ExperimentDBInteractionFailedException, IllegalExperimentSetupException {
		File configFile = new File("testrsc/mlexample/setup.properties");
		IExampleMCCConfig m = (IExampleMCCConfig)ConfigCache.getOrCreate(IExampleMCCConfig.class).loadPropertiesFromFile(configFile);
		IDatabaseConfig dbconfig = (IDatabaseConfig)ConfigFactory.create(IDatabaseConfig.class).loadPropertiesFromFile(configFile);
		if (m.getDatasetFolder() == null || !m.getDatasetFolder().exists()) {
			throw new IllegalArgumentException("config specifies invalid dataset folder " + m.getDatasetFolder());
		}

		ExperimentRunner runner = new ExperimentRunner(m, new IExperimentSetEvaluator() {

			@Override
			public void evaluate(final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor) {

				/* get experiment setup */
				Map<String, String> description = experimentEntry.getExperiment().getValuesOfKeyFields();
				String classifierName = description.get("classifier");
				String datasetName = description.get("dataset");
				int seed = Integer.valueOf(description.get("seed"));

				/* create objects for experiment */
				// load instances for datasetName
				// build Classifier
				// Evaluate Clasifier
				System.out.println("Evaluate " + classifierName + " for dataset " + datasetName + " and seed " + seed);

				/* run experiment */
				Map<String, Object> results = new HashMap<>();
				double loss = 0.1;

				/* report results */
				results.put("loss", loss);
				processor.processResults(results);
			}
		}, new ExperimenterSQLHandle(dbconfig));
		runner.randomlyConductExperiments(true);
	}

}
