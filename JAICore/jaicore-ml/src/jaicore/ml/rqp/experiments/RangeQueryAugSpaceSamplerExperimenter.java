package jaicore.ml.rqp.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.aeonbits.owner.ConfigCache;

import jaicore.basic.SQLAdapter;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import jaicore.ml.rqp.ChooseKAugSpaceSampler;
import jaicore.ml.rqp.IAugmentedSpaceSampler;
import jaicore.ml.rqp.KNNAugSpaceSampler;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.neighboursearch.BallTree;

public class RangeQueryAugSpaceSamplerExperimenter {

	public static void main(String[] args) {
		IRQPAugSpaceSamplerConfig m = ConfigCache.getOrCreate(IRQPAugSpaceSamplerConfig.class);
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
				String samplerName = description.get("sampler");
				String datasetName = description.get("dataset");
				int seed = Integer.valueOf(description.get("seed"));
				int k = Integer.valueOf(description.get("K"));
				String noise = description.get("noise");

				/* create objects for experiment */
				Classifier c_min = AbstractClassifier.forName(classifierName, new String[] {});
				Classifier c_max = AbstractClassifier.forName(classifierName, new String[] {});
				Instances dataTrain = new Instances(new BufferedReader(
						new FileReader(new File(m.getDatasetFolder() + File.separator + datasetName + "_noise_" + noise + "_RQPtrain.arff"))));
				Instances dataTest = new Instances(new BufferedReader(
						new FileReader(new File(m.getDatasetFolder() + File.separator + datasetName + "_RQPtest.arff"))));
				Random rng = new Random(seed);
				
				/* run experiment */
				long startTimeSampling = System.currentTimeMillis();
				IAugmentedSpaceSampler sampler = null;
				switch (samplerName) {
				case "ChooseK":
					sampler = new ChooseKAugSpaceSampler(dataTrain, rng, k);
					break;
				case "KNNExact":
					sampler = new KNNAugSpaceSampler(dataTrain, rng, k, new BallTree());
					break;
				default:
					throw new Exception("Invalid sampler name.");
				}
				
				int numInstances = dataTrain.size();
				Instances augDataTrainMin = new Instances(dataTest, numInstances);
				for(int i = 0; i < numInstances; i++) {
					augDataTrainMin.add(sampler.augSpaceSample());
				}
				long endTimeSampling = System.currentTimeMillis();
				
				long startTimeTrain = System.currentTimeMillis();
				Instances augDataTrainMax = new Instances(augDataTrainMin);
				augDataTrainMin.setClassIndex(augDataTrainMin.numAttributes() - 2);
				augDataTrainMin.deleteAttributeAt(augDataTrainMin.numAttributes() - 1);
				augDataTrainMax.setClassIndex(augDataTrainMin.numAttributes() - 1);
				augDataTrainMax.deleteAttributeAt(augDataTrainMin.numAttributes() - 2);
				c_min.buildClassifier(augDataTrainMin);
				c_max.buildClassifier(augDataTrainMax);
				long endTimeTrain = System.currentTimeMillis();
				
				Instances dataTestMin = new Instances(dataTest);
				dataTestMin.setClassIndex(dataTest.numAttributes() - 2);
				dataTestMin.deleteAttributeAt(dataTest.numAttributes() - 1);
				dataTest.setClassIndex(dataTest.numAttributes() - 1);
				dataTest.deleteAttributeAt(dataTest.numAttributes() - 2);
				
				Map<String, Object> results = new HashMap<>();
				Evaluation evalMin = new Evaluation(dataTestMin);
				Evaluation evalMax = new Evaluation(dataTest);
				evalMin.evaluateModel(c_min, dataTestMin);
				evalMax.evaluateModel(c_max, dataTest);
				double loss_min = evalMin.meanAbsoluteError();
				double loss_max = evalMax.meanAbsoluteError();
				List<double[]> predictions = RQPExperimentUtil.generateMinMaxPredictions(c_min, c_max, dataTestMin, dataTest);
				double wrong_order = RQPExperimentUtil.countMinMaxWrongOrder(predictions) / ((double) dataTest.size());
				double[] evalsSwappedOrder = RQPExperimentUtil.adjustedPredictionsEvaluation(predictions, dataTestMin, dataTest);

				/* report results */
				results.put("sampletime", endTimeSampling - startTimeSampling);
				results.put("traintime", endTimeTrain - startTimeTrain);
				results.put("l1_loss_min", loss_min);
				results.put("l1_loss_max", loss_max);
				results.put("l1_loss_min_swapped", evalsSwappedOrder[0]);
				results.put("l1_loss_max_swapped", evalsSwappedOrder[1]);
				results.put("wrong_order", wrong_order);
				processor.processResults(results);
			}
		});
		runner.randomlyConductExperiments(true);
	}

}
