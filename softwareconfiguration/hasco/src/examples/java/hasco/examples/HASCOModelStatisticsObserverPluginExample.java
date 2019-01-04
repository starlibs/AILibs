package hasco.examples;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.gui.statsplugin.HASCOModelStatisticsPlugin;
import hasco.serialization.UnresolvableRequiredInterfaceException;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirst;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstFactory;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class HASCOModelStatisticsObserverPluginExample {
	public static void main(final String[] args) throws UnresolvableRequiredInterfaceException, IOException, InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		HASCOViaFDAndBestFirstFactory<Double> hascoFactory = new HASCOViaFDAndBestFirstFactory<>(n -> 0.0);
		Random r = new Random();
		// hascoFactory.setProblemInput(new RefinementConfiguredSoftwareConfigurationProblem<>(new File("testrsc/simpleproblemwithtwocomponents.json"), "IFace", n -> System.currentTimeMillis() * 1.0));
		hascoFactory.setProblemInput(new RefinementConfiguredSoftwareConfigurationProblem<>(new File("testrsc/mediumrecursiveproblem.json"), "IFace", n -> r.nextDouble()));
		HASCOViaFDAndBestFirst<Double> hasco = hascoFactory.getAlgorithm();
		hasco.setNumCPUs(1);
		new JFXPanel();
		Platform.runLater(new AlgorithmVisualizationWindow(hasco, new GraphViewPlugin(), new NodeInfoGUIPlugin<>(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())), new HASCOModelStatisticsPlugin()));
		hasco.call();
	}
}
