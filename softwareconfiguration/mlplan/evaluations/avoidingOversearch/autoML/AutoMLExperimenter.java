package avoidingOversearch.autoML;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.aeonbits.owner.ConfigCache;

import de.upb.crc901.mlplan.multiclass.DefaultPreorder;
import de.upb.crc901.mlplan.multiclass.MLPlan;
import jaicore.basic.SQLAdapter;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import jaicore.ml.WekaUtil;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class AutoMLExperimenter {

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
				String algorithmName = description.get("algorithm");
				String datasetName = description.get("dataset");
				int seed = Integer.valueOf(description.get("seed"));

				// Calculate experiment score
				Instances data = new Instances(new BufferedReader(new FileReader(new File(m.getDatasetFolder() + File.separator + datasetName + ".arff"))));
				data.setClassIndex(data.numAttributes() - 1);
				List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);
				int timeoutInSeconds = 3600;
				MLPlan mlplan = new MLPlan(new File("model/weka/weka-all-autoweka.json"));
				// TODO: Configure search algorithm
				switch (algorithmName) {
					case "ml-plan":
						break;
					case "two-phase":
						break;
					case "pareto":
						break;
					case "awa-star":
						break;
					case "r-star":
						break;
					case "mcts":
						break;
				}
				mlplan.setRandomSeed(seed);
				mlplan.setLoggerName("mlplan");
				mlplan.setTimeout(timeoutInSeconds);
				mlplan.setPortionOfDataForPhase2(.3f);
				mlplan.setNodeEvaluator(new DefaultPreorder());
				mlplan.enableVisualization();
				mlplan.buildClassifier(split.get(0));
				Evaluation eval = new Evaluation(split.get(0));
				eval.evaluateModel(mlplan, split.get(1));
				double score = (100 - eval.pctCorrect()) / 100f;
				
				Map<String, Object> results = new HashMap<>();
				results.put("score", score);
				processor.processResults(results);
			}
		});
		runner.randomlyConductExperiments(true);
	}

}
