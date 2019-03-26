package mlexample;

import java.util.HashMap;
import java.util.Map;

import org.aeonbits.owner.ConfigCache;

import jaicore.basic.SQLAdapter;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;

public class MachineLearningExperimenter {

	public static void main(final String[] args) {
		IExampleMCCConfig m = ConfigCache.getOrCreate(IExampleMCCConfig.class);
		if (m.getDatasetFolder() == null || !m.getDatasetFolder().exists()) {
			throw new IllegalArgumentException("config specifies invalid dataset folder " + m.getDatasetFolder());
		}

		ExperimentRunner runner = new ExperimentRunner(new IExperimentSetEvaluator() {

			@Override
			public IExperimentSetConfig getConfig() {
				return m;
			}

			@Override
			public void evaluate(final ExperimentDBEntry experimentEntry, final SQLAdapter adapter, final IExperimentIntermediateResultProcessor processor) throws Exception {

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
		});
		runner.randomlyConductExperiments(true);
	}

}
