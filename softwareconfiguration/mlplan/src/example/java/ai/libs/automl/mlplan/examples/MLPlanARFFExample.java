package ai.libs.automl.mlplan.examples;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.api4.java.algorithm.TimeOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeDisplayInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionPerformanceTimelinePlugin;
import ai.libs.jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import ai.libs.jaicore.ml.WekaUtil;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.RolloutInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.SearchRolloutHistogramPlugin;
import ai.libs.jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.gui.outofsampleplots.OutOfSampleErrorPlotPlugin;
import ai.libs.mlplan.gui.outofsampleplots.WekaClassifierSolutionCandidateRepresenter;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class MLPlanARFFExample {

	private static final Logger LOGGER = LoggerFactory.getLogger(MLPlanARFFExample.class);

	private static final boolean ACTIVATE_VISUALIZATION = false;

	public static void main(final String[] args) throws Exception {

		/* load data for segment dataset and create a train-test-split */
		File file = new File("testrsc/car.arff");
		Instances data = new Instances(new FileReader(file));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, 0, .7f);

		/* initialize mlplan with a tiny search space, and let it run for 30 seconds */
		AbstractMLPlanBuilder builder = AbstractMLPlanBuilder.forWeka();
		builder.withNodeEvaluationTimeOut(new TimeOut(30, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new TimeOut(10, TimeUnit.SECONDS));
		builder.withTimeOut(new TimeOut(300, TimeUnit.SECONDS));
		builder.withNumCpus(2);

		MLPlan mlplan = new MLPlan(builder, split.get(0));
		mlplan.setPortionOfDataForPhase2(0f);
		mlplan.setLoggerName("mlplan");

		if (ACTIVATE_VISUALIZATION) {
			new JFXPanel();
			AlgorithmVisualizationWindow window = null;
			NodeInfoAlgorithmEventPropertyComputer nodeInfoAlgorithmEventPropertyComputer = new NodeInfoAlgorithmEventPropertyComputer();
			List<AlgorithmEventPropertyComputer> algorithmEventPropertyComputers = Arrays.asList(nodeInfoAlgorithmEventPropertyComputer,
					new NodeDisplayInfoAlgorithmEventPropertyComputer<>(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())), new RolloutInfoAlgorithmEventPropertyComputer(nodeInfoAlgorithmEventPropertyComputer),
					new ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer(new WekaClassifierSolutionCandidateRepresenter()));

			window = new AlgorithmVisualizationWindow(mlplan, algorithmEventPropertyComputers, new GraphViewPlugin(), new NodeInfoGUIPlugin(), new SearchRolloutHistogramPlugin(), new SolutionPerformanceTimelinePlugin(),
					new OutOfSampleErrorPlotPlugin(split.get(0), split.get(1)));

			Platform.runLater(window);
		}

		try {
			long start = System.currentTimeMillis();
			Classifier optimizedClassifier = mlplan.call();
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			LOGGER.info("Finished build of the classifier.");
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Chosen model is: {}", (mlplan.getSelectedClassifier()));
			}
			LOGGER.info("Training time was {}s.", trainTime);

			/* evaluate solution produced by mlplan */
			Evaluation eval = new Evaluation(split.get(0));
			eval.evaluateModel(optimizedClassifier, split.get(1));
			LOGGER.info("Error Rate of the solution produced by ML-Plan: {}. Internally believed error was {}", ((100 - eval.pctCorrect()) / 100f), mlplan.getInternalValidationErrorOfSelectedClassifier());
		} catch (NoSuchElementException e) {
			LOGGER.error("Building the classifier failed.", e);
		}
	}

}
