package jaicore.ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.aeonbits.owner.ConfigCache;
import org.junit.Test;

import jaicore.basic.MySQLAdapter;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.ml.evaluation.MulticlassEvaluator;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class MLExperimentTester {

	@Test
	public void test() {
		ISpecificMLExperimentConfig m = ConfigCache.getOrCreate(ISpecificMLExperimentConfig.class);
		if (m.getDatasetFolder() == null || (!new File(m.getDatasetFolder()).exists()))
			throw new IllegalArgumentException("config specifies invalid dataset folder " + m.getDatasetFolder());

		ExperimentRunner runner = new ExperimentRunner(m, (ExperimentDBEntry experiment, MySQLAdapter adapter) -> {
			Map<String, String> description = experiment.getExperiment().getValuesOfKeyFields();
			Classifier c = AbstractClassifier.forName(description.get("classifier"), null);
			Instances data = new Instances(new BufferedReader(new FileReader(new File(m.getDatasetFolder() + File.separator + description.get("dataset") + ".arff"))));
			data.setClassIndex(data.numAttributes() - 1);
			int seed = Integer.valueOf(description.get("seed"));

			System.out.println(c.getClass().getName());
			Map<String, Object> results = new HashMap<>();
			MulticlassEvaluator eval = new MulticlassEvaluator(new Random(seed));
			double loss = eval.getErrorRateForRandomSplit(c, data, .7f);

			results.put("loss", loss);

			return results;
		});
		runner.randomlyConductExperiments();
	}

}
