package mlexample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.aeonbits.owner.ConfigCache;

import jaicore.basic.SQLAdapter;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class MachineLearningExperimenter {

	public static void main(String[] args) {
		IExampleMCCConfig m = ConfigCache.getOrCreate(IExampleMCCConfig.class);
		if (m.getDatasetFolder() == null || !m.getDatasetFolder().exists())
			throw new IllegalArgumentException("config specifies invalid dataset folder " + m.getDatasetFolder());

		ExperimentRunner runner = new ExperimentRunner(new IExperimentSetEvaluator() {

			@Override
			public IExperimentSetConfig getConfig() {
				return m;
			}

			@Override
			public void evaluate(ExperimentDBEntry experimentEntry, SQLAdapter adapter,
					IExperimentIntermediateResultProcessor processor) throws Exception {

				/* get experiment setup */
				Map<String, String> description = experimentEntry.getExperiment().getValuesOfKeyFields();
				String classifierName = description.get("classifier");
				String datasetName = description.get("dataset");
				int seed = Integer.valueOf(description.get("seed"));

				/* create objects for experiment */
				Classifier c = AbstractClassifier.forName(classifierName, new String[] {});
				Instances data = new Instances(new BufferedReader(
						new FileReader(new File(m.getDatasetFolder() + File.separator + datasetName + ".arff"))));
				data.setClassIndex(data.numAttributes() - 1);

				/* run experiment */
				System.out.println(c.getClass().getName());
				Map<String, Object> results = new HashMap<>();
				Evaluation eval = new Evaluation(data);
				eval.crossValidateModel(c, data, 10, new Random(seed));
				double loss = eval.errorRate();

				/* report results */
				results.put("loss", loss);
				processor.processResults(results);
			}
		});
		runner.randomlyConductExperiments(true);
	}

}
