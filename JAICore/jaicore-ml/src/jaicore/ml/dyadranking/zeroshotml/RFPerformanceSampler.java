package jaicore.ml.dyadranking.zeroshotml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aeonbits.owner.ConfigCache;

import jaicore.basic.SQLAdapter;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import jaicore.ml.WekaUtil;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Generates performance samples in terms of the relative 0/1 loss for a random forest classifier
 * on stratified 0.8/0.2 splits from a grid search over the numerical hyper-parameters.
 * 
 * P: bag size
 * I: number of iterations (i.e. number of trees)
 * K: fraction of attributes to randomly investigate
 * M: minimum number of instances per leaf
 * V: minimum variance for split (in terms of exponents 1e^V
 * depth: max depth of trees
 * N: num folds for backfitting
 * 
 * @author michael
 *
 */
public class RFPerformanceSampler {
	
	private static Map<String, String> datasetIdMap = new HashMap<String, String>() {{
				put("12", "dataset_12_mfeat-factors.arff");
				put("14", "dataset_14_mfeat-fourier.arff");
				put("16", "dataset_16_mfeat-karhunen.arff");
				put("18", "dataset_18_mfeat-morphological.arff");
				put("20", "dataset_20_mfeat-pixel.arff");
				put("21", "dataset_21_car.arff");
				put("22", "dataset_22_mfeat-zernike.arff");
				put("23", "dataset_23_cmc.arff");
				put("24", "dataset_24_mushroom.arff");
				put("26", "dataset_26_nursery.arff");
				put("28", "dataset_28_optdigits.arff");
				put("3", "dataset_3_kr-vs-kp.arff");
				put("30", "dataset_30_page-blocks.arff");
				put("32", "dataset_32_pendigits.arff");
		}};
	
	public static void main(String[] args) {
		IRFPerformanceSamplerConfig m = ConfigCache.getOrCreate(IRFPerformanceSamplerConfig.class);
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
				
				String dataset = description.get("dataset");
				Instances data = new Instances(new BufferedReader(
						new FileReader(new File(m.getDatasetFolder() 
								+ File.separator 
								+ datasetIdMap.get(dataset)))));
				data.setClassIndex(data.numAttributes() - 1);
				
				String P_bag_size_option = "-P " + description.get("P_bag_size");
				String I_iteration_option = " -I " + description.get("I_iterations");
				String M_num_instances_option = " -M " + description.get("M_num_instances");
				String V_min_var_option = " -V " + Math.exp(Integer.parseInt(description.get("V_min_var_exp")));
				String depth_option = " -depth " + description.get("depth");
				String N_backfitting_folds_option = " -N "  + description.get("N_backfitting_folds");				
				int K_num_attributes = (int) Math.ceil(data.numAttributes() * Double.parseDouble(description.get("K_fraction_attributes")));
				String K_option = " -K " + K_num_attributes;
				
				List<Instances> dataSplit = WekaUtil.getStratifiedSplit(data, 0, 0.8, 0.2);
				Instances dataTrain = dataSplit.get(0);
				Instances dataTest = dataSplit.get(1);
				
				RandomForest rf = new RandomForest();
				String options = 
						P_bag_size_option +
						I_iteration_option +
						K_option +
						M_num_instances_option +
						V_min_var_option +
						depth_option +
						N_backfitting_folds_option;
				String[] optionsSplit = Utils.splitOptions(options);
				rf.setOptions(optionsSplit);
				
				rf.buildClassifier(dataTrain);
				Evaluation eval = new Evaluation(dataTest);
				eval.evaluateModel(rf, dataTest);
				double performance = eval.pctCorrect();

				Map<String, Object> results = new HashMap<>();
				results.put("performance", performance);
				processor.processResults(results);
			}
		});
		runner.randomlyConductExperiments(true);
	}
}
