package ai.libs.hyperopt.example.automl.weka;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.Component;
import ai.libs.hasco.serialization.ComponentLoader;
import ai.libs.hyperopt.example.hyperopt.PCSBasedOptimizationRunner;
import ai.libs.hyperopt.example.mlplan.gui.outofsampleplots.OutOfSampleErrorPlotPlugin;
import ai.libs.hyperopt.optimizer.pcs.HyperBandOptimizer;
import ai.libs.hyperopt.optimizer.pcs.PCSBasedOptimizerInput;
import ai.libs.hyperopt.optimizer.pcs.converter.ComponentToPCSConverterUtil;
import ai.libs.hyperopt.util.PCSBasedOptimizationSolutionCandidateRepresenter;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeDisplayInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionPerformanceTimelinePlugin;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.RolloutInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.SearchRolloutHistogramPlugin;
import ai.libs.jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
import ai.libs.mlplan.multiclass.wekamlplan.weka.WekaPipelineFactory;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class PCSBasedOptimizationRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(PCSBasedOptimizationRunner.class);

	public static void main(final String[] args) throws Exception {
		// initialize
		File HASCOFileInput = new File("../mlplan/resources/automl/searchmodels/weka/autoweka.json");
		ComponentLoader cl = new ComponentLoader(HASCOFileInput);
		Collection<Component> components = cl.getComponents();
		String requestedInterface = "BaseClassifier";
		String requestedComponent = "";
		PCSBasedOptimizerInput input = new PCSBasedOptimizerInput(components, requestedComponent, requestedInterface);
		WekaPipelineFactory classifierFactory = new WekaPipelineFactory();
		ComponentInstanceEvaluator evaluator = new ComponentInstanceEvaluator(classifierFactory, "testrsc/iris.arff", "HyperBandOptimizer");

		// generate PCS files
		ComponentToPCSConverterUtil.generatePCSFile(input, "PCSBasedOptimizerScripts/HyperBandOptimizer/");

		// optimization
		HyperBandOptimizer optimizer = HyperBandOptimizer.HyperBandOptimizerBuilder(input, evaluator).withExecutionPath("PCSBasedOptimizerScripts/HyperBandOptimizer").maxBudget(230.0).minBudget(9.0).nIterations(4).build();

		new JFXPanel();
		OptimizerVisualizationWindow window = null;
		NodeInfoAlgorithmEventPropertyComputer nodeInfoAlgorithmEventPropertyComputer = new NodeInfoAlgorithmEventPropertyComputer();
		List<AlgorithmEventPropertyComputer> algorithmEventPropertyComputers = Arrays.asList(nodeInfoAlgorithmEventPropertyComputer,
				new NodeDisplayInfoAlgorithmEventPropertyComputer<>(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())), new RolloutInfoAlgorithmEventPropertyComputer(nodeInfoAlgorithmEventPropertyComputer),
				new ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer(new PCSBasedOptimizationSolutionCandidateRepresenter()));

		window = new OptimizerVisualizationWindow(evaluator, algorithmEventPropertyComputers, new GraphViewPlugin(), new NodeInfoGUIPlugin(), new SearchRolloutHistogramPlugin(), new SolutionPerformanceTimelinePlugin(),
				new OutOfSampleErrorPlotPlugin(evaluator.getInstances().get(0), evaluator.getInstances().get(1)));

		Platform.runLater(window);

		optimizer.optimize();

	}

}