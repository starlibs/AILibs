package ai.libs.mlplan.examples.multiclass.weka;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.algorithm.Timeout;

import ai.libs.hasco.gui.civiewplugin.TFDNodeAsCIViewInfoGenerator;
import ai.libs.hasco.gui.statsplugin.HASCOSolutionCandidateRepresenter;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.graphvisualizer.events.recorder.AlgorithmEventHistory;
import ai.libs.jaicore.graphvisualizer.events.recorder.AlgorithmEventHistoryRecorder;
import ai.libs.jaicore.graphvisualizer.events.recorder.AlgorithmEventHistorySerializer;
import ai.libs.jaicore.graphvisualizer.plugin.IComputedGUIPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionPerformanceTimelinePlugin;
import ai.libs.jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.SearchRolloutHistogramPlugin;
import ai.libs.jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.weka.MLPlanWekaBuilder;
import javafx.embed.swing.JFXPanel;

public class MLPlanEventHistorySerializationAndLoadingExample {

	public static void main(final String[] args) throws Exception {

		// ILabeledDataset<?> ds = OpenMLDatasetReader.deserializeDataset(346);
		File datasetFile = new File("testrsc/car.arff");
		System.out.println(datasetFile.getAbsolutePath());

		ILabeledDataset<?> ds = ArffDatasetAdapter.readDataset(datasetFile);

		List<ILabeledDataset<?>> split = SplitterUtil.getLabelStratifiedTrainTestSplit(ds, new Random(1), .7);

		/* initialize mlplan, and let it run for 5 minute */
		MLPlanWekaBuilder mlplanBuilder = new MLPlanWekaBuilder().withNumCpus(4).withTimeOut(new Timeout(2, TimeUnit.MINUTES)).withCandidateEvaluationTimeOut(new Timeout(10, TimeUnit.SECONDS))
				.withNodeEvaluationTimeOut(new Timeout(30, TimeUnit.SECONDS)).withDataset(split.get(0));
		MLPlan<IWekaClassifier> mlplan = mlplanBuilder.build();

		/* setup event history recorder and register it in ML-Plan*/
		AlgorithmEventHistoryRecorder eventHistoryRecorder = new AlgorithmEventHistoryRecorder();

		/* register the property computers of all plugins which should be used for visualizing the recorded history later*/
		new JFXPanel(); // dummy to initialize JavaFX if this has not happened before
		List<IGUIPlugin> pluginsWhichShouldBeUsedLaterForVisualization = Arrays.asList(new NodeInfoGUIPlugin(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())), new SearchRolloutHistogramPlugin(),
				new NodeInfoGUIPlugin(new TFDNodeAsCIViewInfoGenerator(mlplanBuilder.getComponents())), new SolutionPerformanceTimelinePlugin(new HASCOSolutionCandidateRepresenter()));
		for (IGUIPlugin plugin : pluginsWhichShouldBeUsedLaterForVisualization) {
			if (plugin instanceof IComputedGUIPlugin) {
				eventHistoryRecorder.addPropertyComputer(((IComputedGUIPlugin) plugin).getPropertyComputers());
			}
		}

		/* register event history recorder in ML-Plan*/
		mlplan.registerListener(eventHistoryRecorder);

		try {
			long start = System.currentTimeMillis();
			IWekaClassifier optimizedClassifier = mlplan.call();
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			System.out.println("Finished build of the classifier. Training time was " + trainTime + "s.");

			/* evaluate solution produced by mlplan */
			SupervisedLearnerExecutor executor = new SupervisedLearnerExecutor();
			ILearnerRunReport report = executor.execute(optimizedClassifier, split.get(1));
			System.out.println("Error Rate of the solution produced by ML-Plan: " + EClassificationPerformanceMeasure.ERRORRATE.loss(report.getPredictionDiffList().getCastedView(Integer.class, ISingleLabelClassification.class)));
		} catch (NoSuchElementException e) {
			System.out.println("Building the classifier failed: " + e.getMessage());
		}

		/* obtain the algorithm history from recorder and create a serializer */
		AlgorithmEventHistory history = eventHistoryRecorder.getHistory();

		AlgorithmEventHistorySerializer serializer = new AlgorithmEventHistorySerializer();

		/* serialize the obtained algorithm history and write it to a file */
		String serializedHistory = serializer.serializeAlgorithmEventHistory(history);
		FileUtil.writeFileAsList(Arrays.asList(serializedHistory), "history.json");

		/* read a serialized history and start an algorithm inspector on this history */
		AlgorithmEventHistory deserializedHistory = serializer.deserializeAlgorithmEventHistory(new File("history.json"));

		AlgorithmVisualizationWindow window = new AlgorithmVisualizationWindow(deserializedHistory);
		window.withMainPlugin(new GraphViewPlugin());
		window.withPlugin(new NodeInfoGUIPlugin(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())), new SearchRolloutHistogramPlugin(), new NodeInfoGUIPlugin(new TFDNodeAsCIViewInfoGenerator(mlplanBuilder.getComponents())),
				new SolutionPerformanceTimelinePlugin(new HASCOSolutionCandidateRepresenter()));
	}
}
