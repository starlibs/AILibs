package ai.libs.mlplan.examples.multiclass.sklearn;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.algorithm.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import ai.libs.jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.SearchRolloutHistogramPlugin;
import ai.libs.jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.sklearn.builder.MLPlanScikitLearnBuilder;

public class MLPlanScikitLearnGraphVisualizationExample {

	private static final Logger LOGGER = LoggerFactory.getLogger("example");

	public static void main(final String[] args) throws Exception {

		ILabeledDataset<?> ds = OpenMLDatasetReader.deserializeDataset(346);
		List<ILabeledDataset<?>> split = SplitterUtil.getLabelStratifiedTrainTestSplit(ds, new Random(0), .7);

		/* initialize mlplan, and let it run for 1 hour */
		MLPlanScikitLearnBuilder builder = MLPlanScikitLearnBuilder.forClassification();
		builder.withNumCpus(4);
		builder.withSeed(42);
		builder.withTimeOut(new Timeout(1, TimeUnit.HOURS));
		builder.withDataset(split.get(0));

		MLPlan<ScikitLearnWrapper<IPrediction, IPredictionBatch>> mlplan = builder.build();
		mlplan.setPortionOfDataForPhase2(.3f);
		mlplan.setLoggerName("testedalgorithm");

		/* start visualization */
		AlgorithmVisualizationWindow window = new AlgorithmVisualizationWindow(mlplan);
		window.withMainPlugin(new GraphViewPlugin());
		window.withPlugin(new NodeInfoGUIPlugin(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())), new SearchRolloutHistogramPlugin());

		try {
			long start = System.currentTimeMillis();
			ScikitLearnWrapper<IPrediction, IPredictionBatch> optimizedClassifier = mlplan.call();
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			LOGGER.info("Finished build of the classifier. Training time was {}s.", trainTime);
			LOGGER.info("Chosen model is: {}", (mlplan.getSelectedClassifier()));

			/* evaluate solution produced by mlplan */
			SupervisedLearnerExecutor executor = new SupervisedLearnerExecutor();
			ILearnerRunReport report = executor.execute(optimizedClassifier, split.get(1));
			LOGGER.info("Error Rate of the solution produced by ML-Plan: {}. Internally believed error was {}",
					EClassificationPerformanceMeasure.ERRORRATE.loss(report.getPredictionDiffList().getCastedView(Integer.class, ISingleLabelClassification.class)), mlplan.getInternalValidationErrorOfSelectedClassifier());
		} catch (NoSuchElementException e) {
			LOGGER.error("Building the classifier failed: {}", LoggerUtil.getExceptionInfo(e));
		}
	}
}
