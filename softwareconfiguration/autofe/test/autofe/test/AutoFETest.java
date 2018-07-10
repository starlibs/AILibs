package autofe.test;

import java.io.File;

import org.slf4j.Logger;

import autofe.algorithm.hasco.evaluation.AbstractHASCOFENodeEvaluator;
import de.upb.crc901.mlplan.multiclass.DefaultPreorder;
import de.upb.crc901.mlplan.multiclass.MLPlan;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.structure.core.Node;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class AutoFETest {
	public static final String API_KEY = "4350e421cdc16404033ef1812ea38c01";

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

	public static AbstractHASCOFENodeEvaluator getRandomNodeEvaluator(final int maxPipelineSize) {
		return new AbstractHASCOFENodeEvaluator(maxPipelineSize) {

			@Override
			public Double f(Node<TFDNode, ?> node) throws Throwable {
				if (node.getParent() == null)
					return null;

				// If pipeline is too deep, assign worst value
				if (node.path().size() > this.maxPipelineSize)
					return 1.0;

				return null;
			}
		};
	}
}
