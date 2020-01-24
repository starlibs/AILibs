package ai.libs.jaicore.ml;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.aeonbits.owner.ConfigCache;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.experiments.ExperimentDBEntry;
import ai.libs.jaicore.experiments.ExperimentRunner;
import ai.libs.jaicore.experiments.IExperimentIntermediateResultProcessor;
import ai.libs.jaicore.experiments.IExperimentSetEvaluator;
import ai.libs.jaicore.experiments.databasehandle.ExperimenterMySQLHandle;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SingleRandomSplitClassifierEvaluator;
import ai.libs.jaicore.ml.weka.classification.learner.WekaClassifier;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * Test for the experiment package in the context of ml experiments.
 *
 * @author fmohr, mwever
 */
public class MLExperimentTester implements IExperimentSetEvaluator {

	private static final ISpecificMLExperimentConfig config = ConfigCache.getOrCreate(ISpecificMLExperimentConfig.class);
	private static final Logger logger = LoggerFactory.getLogger(MLExperimentTester.class);
	private boolean conductedExperiment = false;

	@Override
	public void evaluate(final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor) throws ExperimentEvaluationFailedException {
		try {
			if (config.getDatasetFolder() == null || (!config.getDatasetFolder().exists())) {
				throw new IllegalArgumentException("config specifies invalid dataset folder " + config.getDatasetFolder());
			}
			Map<String, String> description = experimentEntry.getExperiment().getValuesOfKeyFields();
			Classifier c = AbstractClassifier.forName(description.get("classifier"), null);
			Instances data = new Instances(new BufferedReader(new FileReader(new File(config.getDatasetFolder() + File.separator + description.get("dataset") + ".arff"))));
			data.setClassIndex(data.numAttributes() - 1);
			int seed = Integer.parseInt(description.get("seed"));

			logger.info("Testing classifier {}", c.getClass().getName());
			Map<String, Object> results = new HashMap<>();

			ILabeledDataset<? extends ILabeledInstance> dataset = new WekaInstances(data);
			SingleRandomSplitClassifierEvaluator eval = new SingleRandomSplitClassifierEvaluator(dataset, .7, new Random(seed));
			double loss = eval.evaluate(new WekaClassifier(c));

			results.put("loss", loss);
			processor.processResults(results);
			this.conductedExperiment = true;
		} catch (Exception e) {
			throw new ExperimentEvaluationFailedException(e);
		}
	}

	@Test
	public void testExperimentRunnerForMLExperiment() throws ExperimentDBInteractionFailedException, InterruptedException {
		ExperimentRunner runner = new ExperimentRunner(config, new MLExperimentTester(), new ExperimenterMySQLHandle(config));
		runner.randomlyConductExperiments();
		assertTrue(this.conductedExperiment);
	}
}
