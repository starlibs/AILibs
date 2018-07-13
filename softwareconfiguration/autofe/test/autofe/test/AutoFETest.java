package autofe.test;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;

import de.upb.crc901.mlplan.multiclass.DefaultPreorder;
import de.upb.crc901.mlplan.multiclass.MLPlan;
import jaicore.ml.WekaUtil;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class AutoFETest {

	public static double evaluateMLPlan(final int timeout, final Instances training, final Instances test,
			final Logger logger) throws Exception {

		logger.debug("Starting ML-Plan execution. Training on " + training.numInstances() + " instances with "
				+ training.numAttributes() + " attributes.");

		/* Initialize MLPlan using WEKA components */
		MLPlan mlplan = new MLPlan(new File("model/mlplan_weka/weka-all-autoweka.json"));
		mlplan.setNumberOfCPUs(Runtime.getRuntime().availableProcessors());
		mlplan.setLoggerName("mlplan");
		mlplan.setTimeout(timeout);
		mlplan.setPortionOfDataForPhase2(.3f);
		mlplan.setNodeEvaluator(new DefaultPreorder());
		mlplan.enableVisualization();
		mlplan.buildClassifier(training);

		/* evaluate solution produced by mlplan */
		Evaluation eval = new Evaluation(training);
		eval.evaluateModel(mlplan, test);

		return eval.pctCorrect();
	}

	public static double evaluateMLPlan(final int timeout, final Instances instances, final double trainRatio,
			final Logger logger) throws Exception {

		List<Instances> split = WekaUtil.getStratifiedSplit(instances, new Random(42), trainRatio);

		return evaluateMLPlan(timeout, split.get(0), split.get(1), logger);
	}

}
