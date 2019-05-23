package de.upb.crc901.mlplan.examples.multilabel.meka;

import java.io.FileReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;

import de.upb.crc901.mlplan.core.AbstractMLPlanBuilder;
import de.upb.crc901.mlplan.core.MLPlan;
import de.upb.crc901.mlplan.core.MLPlanMekaBuilder;
import de.upb.crc901.mlplan.gui.outofsampleplots.OutOfSampleErrorPlotPlugin;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import hasco.gui.statsplugin.HASCOModelStatisticsPlugin;
import jaicore.basic.TimeOut;
import jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionPerformanceTimelinePlugin;
import jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import jaicore.ml.weka.dataset.splitter.ArbitrarySplitter;
import jaicore.ml.weka.dataset.splitter.IDatasetSplitter;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import jaicore.search.gui.plugins.rollouthistograms.SearchRolloutHistogramPlugin;
import jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
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