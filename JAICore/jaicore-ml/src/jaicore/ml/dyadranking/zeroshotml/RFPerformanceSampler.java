package jaicore.ml.dyadranking.zeroshotml;

import java.util.Map;

import org.aeonbits.owner.ConfigCache;

import jaicore.basic.SQLAdapter;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import weka.classifiers.trees.RandomForest;

public class RFPerformanceSampler {

	public static void main(String[] args) {
		IRFPerformanceSamplerConfig m = ConfigCache.getOrCreate(IRFPerformanceSamplerConfig.class);
	//	if (m.getDatasetFolder() == null || !m.getDatasetFolder().exists())
	//		throw new IllegalArgumentException("config specifies invalid dataset folder " + m.getDatasetFolder());

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
				String P_bag_size_option = "-P " + description.get("P_bag_size");
				String I_iteration_option = "-I" + description.get("I_iteration");
				String M_num_instances_option = "-M " + description.get("M_num_instances");
				String V_min_var_option = "-V " + Math.exp(Integer.parseInt(description.get("V_min_var_exp")));
				String depth_option = "-depth" + description.get("depth");
				String N_backfitting_folds_option = "-N "  + description.get("N_backfitting_folds");
				
				double K_fraction_attributes = Double.parseDouble(description.get("K_fraction_attributes"));
				
				RandomForest rf = new RandomForest();
				
				String[] options = new String[]{
						P_bag_size_option,
						I_iteration_option,
						M_num_instances_option,
						V_min_var_option,
						depth_option,
						N_backfitting_folds_option};
				rf.setOptions(options);
				
				// TODO Auto-generated method stub
			}
		});
		runner.randomlyConductExperiments(true);
	}
}
