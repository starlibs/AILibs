package jaicore.ml;

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
import jaicore.ml.evaluation.MulticlassEvaluator;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class MLExperimentTester implements IExperimentSetEvaluator {

	private final ISpecificMLExperimentConfig config = ConfigCache.getOrCreate(ISpecificMLExperimentConfig.class);

	@Override
	public IExperimentSetConfig getConfig() {
		return config;
	}

	@Override
	public void evaluate(ExperimentDBEntry experimentEntry, SQLAdapter adapter, IExperimentIntermediateResultProcessor processor) throws Exception {
		if (config.getDatasetFolder() == null || (!new File(config.getDatasetFolder()).exists()))
			throw new IllegalArgumentException("config specifies invalid dataset folder " + config.getDatasetFolder());
		Map<String, String> description = experimentEntry.getExperiment().getValuesOfKeyFields();
		Classifier c = AbstractClassifier.forName(description.get("classifier"), null);
		Instances data = new Instances(new BufferedReader(new FileReader(new File(config.getDatasetFolder() + File.separator + description.get("dataset") + ".arff"))));
		data.setClassIndex(data.numAttributes() - 1);
		int seed = Integer.valueOf(description.get("seed"));

		System.out.println(c.getClass().getName());
		Map<String, Object> results = new HashMap<>();
		MulticlassEvaluator eval = new MulticlassEvaluator(new Random(seed));
		double loss = eval.getErrorRateForRandomSplit(c, data, .7f);

		results.put("loss", loss);
		processor.processResults(results);
		
	}

	public static void main(String[] args) {
		ExperimentRunner runner = new ExperimentRunner(new MLExperimentTester());
		runner.randomlyConductExperiments();
	}
	
}
