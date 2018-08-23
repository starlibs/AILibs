package de.upb.crc901.mlplan.examples.wekaRunner;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.automl.hascocombinedml.HASCOForCombinedMLConfig;
import de.upb.crc901.automl.hascoml.HASCOMLTwoPhaseSelection;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.ml.WekaUtil;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.search.structure.core.Node;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class MLPlanRunnerWEKA {
	private static final Logger logger = LoggerFactory.getLogger(MLPlanRunnerWEKA.class);
	private static final MLPlanConfig EXP_CONFIG = ConfigCache.getOrCreate(MLPlanConfig.class);
	private static final HASCOForWekaMLConfig ALGO_CONFIG = ConfigCache.getOrCreate(HASCOForWekaMLConfig.class);

	public static void main(final String[] args) throws Exception {
		if (args.length > 0 && args[0].equals("-h")) {
			System.out.println("Parameters to set: ");
			System.out.println("<dataset_file> <global_timeout> <evaluation_timeout>");
			System.exit(0);
		}

		int seed = 0;
		/* set dataset file if given */
		if (args.length > 0) {
			EXP_CONFIG.setProperty(MLPlanConfig.K_MLPLAN_DATASET_FILE, args[0]);
		}
		/* set global timeout, if given */
		if (args.length > 1) {
			EXP_CONFIG.setProperty(MLPlanConfig.K_MLPLAN_TIMEOUT, args[1]);
		}
		/* set timeout for single evaluation, if given */
		if (args.length > 2) {
			EXP_CONFIG.setProperty(MLPlanConfig.K_MLPLAN_EVAL_TIMEOUT, args[2]);
		}

		/* set ports for pipeline plans */
		Instances data = new Instances(new FileReader(EXP_CONFIG.getDatasetFile()));
		data.setClassIndex(data.numAttributes() - 1);

		/* extract all relevant information about the experiment */
		ALGO_CONFIG.setProperty(HASCOForWekaMLConfig.K_RUN_START_TIMESTAMP, System.currentTimeMillis() + "");
		ALGO_CONFIG.setProperty(HASCOForWekaMLConfig.K_SEED, "" + seed);
		System.out.println(EXP_CONFIG.getEvalTimeout());

		ALGO_CONFIG.setProperty(HASCOForCombinedMLConfig.K_EVAL_TIMEOUT, EXP_CONFIG.getEvalTimeout() + "");

		/* read data and create benchmarks that evaluate solutions */
		List<Instances> testSplit = WekaUtil.getStratifiedSplit(data, new Random(seed), 0.7);

		HASCOMLTwoPhaseSelection mlPlan = new HASCOMLTwoPhaseSelection(new File("model/weka/weka-all-autoweka.json"));
		mlPlan.setNumberOfCPUs(EXP_CONFIG.getNumberOfCPUs());
		mlPlan.setNumberOfConsideredSolutions(100);
		mlPlan.setRandomSeed((int) ALGO_CONFIG.getSeed());
		mlPlan.setTimeout(EXP_CONFIG.getTimeout());
		mlPlan.setTimeoutPerNodeFComputation(EXP_CONFIG.getEvalTimeout());

		/* run algorithm */
		if (ALGO_CONFIG.getShowGraphVisualization()) {
			new SimpleGraphVisualizationWindow<Node<TFDNode, Double>>(mlPlan).getPanel()
					.setTooltipGenerator(new TFDTooltipGenerator<>());
		}

		mlPlan.buildClassifier(testSplit.get(0));

		System.out.println("Done");
		System.out.println("Best Solution found:");
		System.out.println(mlPlan.getSelectedClassifier());
		Evaluation eval = new Evaluation(data);
		eval.evaluateModel(mlPlan, testSplit.get(1), new Object[] {});

		System.out.println("Accuracy: " + eval.pctCorrect() / 100);
		System.out.println("Error Rate: " + eval.errorRate());
		System.out.println("Unweighted Macro F Measure: " + eval.unweightedMacroFmeasure());
		System.out.println("Weighted F Measure: " + eval.weightedFMeasure());

	}

}
