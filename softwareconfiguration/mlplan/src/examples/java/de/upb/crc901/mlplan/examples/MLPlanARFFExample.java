package de.upb.crc901.mlplan.examples;

import java.io.FileReader;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import de.upb.crc901.mlplan.core.MLPlan;
import de.upb.crc901.mlplan.core.MLPlanBuilder;
import de.upb.crc901.mlplan.gui.outofsampleplots.OutOfSampleErrorPlotPlugin;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import hasco.gui.civiewplugin.TFDNodeAsCIViewInfoGenerator;
import jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionPerformanceTimelinePlugin;
import jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import jaicore.ml.WekaUtil;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import jaicore.search.gui.plugins.rollouthistograms.SearchRolloutHistogramPlugin;
import jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class MLPlanARFFExample {

	public static void main(final String[] args) throws Exception {

		/* load data for segment dataset and create a train-test-split */
		Instances data = new Instances(new FileReader("testrsc/heart.statlog.arff"));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, 0, .7f);

		/* initialize mlplan with a tiny search space, and let it run for 30 seconds */
		MLPlan mlplan = new MLPlan(new MLPlanBuilder().withAutoWEKAConfiguration(), split.get(0));
		mlplan.setPortionOfDataForPhase2(0.3f);
		mlplan.setLoggerName("mlplan");
		mlplan.setTimeout(300, TimeUnit.SECONDS);
		mlplan.setTimeoutForNodeEvaluation(15);
		mlplan.setTimeoutForSingleSolutionEvaluation(15);
		mlplan.setNumCPUs(3);

		/* open visualization */
		new JFXPanel();
		AlgorithmVisualizationWindow window = new AlgorithmVisualizationWindow(mlplan, new GraphViewPlugin(), new NodeInfoGUIPlugin<>(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())), new SearchRolloutHistogramPlugin<>(),
				new SolutionPerformanceTimelinePlugin(), new OutOfSampleErrorPlotPlugin(split.get(0), split.get(1)), new NodeInfoGUIPlugin<>(new TFDNodeAsCIViewInfoGenerator(mlplan.getComponents()), "Pipeline"));
		Platform.runLater(window);

		try {
			long start = System.currentTimeMillis();
			Classifier optimizedClassifier = mlplan.call();
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			System.out.println("Finished build of the classifier.");
			System.out.println("Chosen model is: " + ((MLPipeline) mlplan.getSelectedClassifier()).toString());
			System.out.println("Training time was " + trainTime + "s.");

			/* evaluate solution produced by mlplan */
			Evaluation eval = new Evaluation(split.get(0));
			eval.evaluateModel(optimizedClassifier, split.get(1));
			System.out.println("Error Rate of the solution produced by ML-Plan: " + ((100 - eval.pctCorrect()) / 100f) + ". Internally believed error was " + mlplan.getInternalValidationErrorOfSelectedClassifier());
		} catch (NoSuchElementException e) {
			System.out.println("Building the classifier failed: " + e.getMessage());
		}
	}

}
