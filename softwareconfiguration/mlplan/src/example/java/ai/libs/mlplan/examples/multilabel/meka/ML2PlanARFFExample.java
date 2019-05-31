package ai.libs.mlplan.examples.multilabel.meka;

import java.io.FileReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;

import ai.libs.hasco.gui.statsplugin.HASCOModelStatisticsPlugin;
import ai.libs.jaicore.basic.TimeOut;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionPerformanceTimelinePlugin;
import ai.libs.jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import ai.libs.jaicore.ml.weka.dataset.splitter.ArbitrarySplitter;
import ai.libs.jaicore.ml.weka.dataset.splitter.IDatasetSplitter;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.SearchRolloutHistogramPlugin;
import ai.libs.jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.core.MLPlanMekaBuilder;
import ai.libs.mlplan.gui.outofsampleplots.OutOfSampleErrorPlotPlugin;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import meka.core.MLUtils;
import weka.core.Instances;

/**
 * Example demonstrating the usage of Ml2Plan (MLPlan for multilabel classification).
 *
 * @author mwever, helegraf
 *
 */
public class ML2PlanARFFExample {

	private static final boolean ACTIVATE_VISUALIZATION = true;

	public static void main(final String[] args) throws Exception {
		/* load data for segment dataset and create a train-test-split */
		Instances data = new Instances(new FileReader("../../../datasets/classification/multi-label/flags.arff"));
		MLUtils.prepareData(data);

		IDatasetSplitter testSplitter = new ArbitrarySplitter();
		List<Instances> split = testSplitter.split(data, 0, .7);

		MLPlanClassifierConfig algoConfig = ConfigFactory.create(MLPlanClassifierConfig.class);
		algoConfig.setProperty(MLPlanClassifierConfig.SELECTION_PORTION, "0.0");

		MLPlanMekaBuilder builder = AbstractMLPlanBuilder.forMeka();
		builder.withAlgorithmConfig(algoConfig);
		builder.withNodeEvaluationTimeOut(new TimeOut(60, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new TimeOut(60, TimeUnit.SECONDS));
		builder.withNumCpus(8);
		builder.withTimeOut(new TimeOut(150, TimeUnit.SECONDS));

		MLPlan ml2plan = new MLPlan(builder, split.get(0));
		ml2plan.setLoggerName("ml2plan");

		if (ACTIVATE_VISUALIZATION) {
			new JFXPanel();
			AlgorithmVisualizationWindow window = new AlgorithmVisualizationWindow(ml2plan, new GraphViewPlugin(), new NodeInfoGUIPlugin<>(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())), new SearchRolloutHistogramPlugin<>(),
					new SolutionPerformanceTimelinePlugin(), new HASCOModelStatisticsPlugin(), new OutOfSampleErrorPlotPlugin(split.get(0), split.get(1)));
			Platform.runLater(window);
		}

		ml2plan.call();
	}
}