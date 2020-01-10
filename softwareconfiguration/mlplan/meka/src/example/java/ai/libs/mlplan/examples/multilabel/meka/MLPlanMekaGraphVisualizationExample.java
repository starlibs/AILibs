package ai.libs.mlplan.examples.multilabel.meka;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.algorithm.Timeout;

import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeDisplayInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionPerformanceTimelinePlugin;
import ai.libs.jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.classification.multilabel.dataset.IMekaInstances;
import ai.libs.jaicore.ml.classification.multilabel.dataset.MekaInstances;
import ai.libs.jaicore.ml.classification.multilabel.learner.IMekaClassifier;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.RolloutInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.SearchRolloutHistogramPlugin;
import ai.libs.jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.multilabel.mekamlplan.MLPlanMekaBuilder;
import javafx.embed.swing.JFXPanel;
import weka.core.Instances;

public class MLPlanMekaGraphVisualizationExample {
	public static void main(final String[] args) throws Exception {

		File datasetFile = new File("../../../../datasets/classification/multi-label/flags.arff");
		IMekaInstances dataset = new MekaInstances(new Instances(new FileReader(datasetFile)));
		List<ILabeledDataset<?>> split = SplitterUtil.getSimpleTrainTestSplit(dataset, new Random(0), .7);

		/* initialize mlplan, and let it run for 1 hour */
		MLPlan<IMekaClassifier> mlplan = new MLPlanMekaBuilder().withNumCpus(4).withTimeOut(new Timeout(1, TimeUnit.HOURS)).withDataset(split.get(0)).build();

		/* start visualization */
		new JFXPanel();
		AlgorithmVisualizationWindow window;
		NodeInfoAlgorithmEventPropertyComputer nodeInfoAlgorithmEventPropertyComputer = new NodeInfoAlgorithmEventPropertyComputer();
		AlgorithmEventPropertyComputer comp = new NodeDisplayInfoAlgorithmEventPropertyComputer<>(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator()));
		List<AlgorithmEventPropertyComputer> algorithmEventPropertyComputers = Arrays.asList(nodeInfoAlgorithmEventPropertyComputer, comp, new RolloutInfoAlgorithmEventPropertyComputer(nodeInfoAlgorithmEventPropertyComputer));
		window = new AlgorithmVisualizationWindow(mlplan, algorithmEventPropertyComputers, new GraphViewPlugin(), new NodeInfoGUIPlugin(), new SearchRolloutHistogramPlugin(), new SolutionPerformanceTimelinePlugin());
		window.show();

		try {
			long start = System.currentTimeMillis();
			IMekaClassifier optimizedClassifier = mlplan.call();
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			System.out.println("Finished build of the classifier. Training time was " + trainTime + "s.");

			/* evaluate solution produced by mlplan */
			SupervisedLearnerExecutor executor = new SupervisedLearnerExecutor();
			ILearnerRunReport report = executor.execute(optimizedClassifier, split.get(1));
			System.out.println("Error Rate of the solution produced by ML-Plan: " + EClassificationPerformanceMeasure.ERRORRATE.loss(report.getPredictionDiffList()));
		} catch (NoSuchElementException e) {
			System.out.println("Building the classifier failed: " + e.getMessage());
		}
	}
}
