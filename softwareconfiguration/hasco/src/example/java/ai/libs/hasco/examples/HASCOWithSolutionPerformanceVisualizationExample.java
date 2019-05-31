package ai.libs.hasco.examples;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import ai.libs.hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.hasco.serialization.UnresolvableRequiredInterfaceException;
import ai.libs.hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirst;
import ai.libs.hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstFactory;
import ai.libs.jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmException;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionPerformanceTimelinePlugin;
import ai.libs.jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class HASCOWithSolutionPerformanceVisualizationExample {
	public static void main(String[] args) throws UnresolvableRequiredInterfaceException, IOException, InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		HASCOViaFDAndBestFirstFactory<Double> hascoFactory = new HASCOViaFDAndBestFirstFactory<>(n -> 0.0);
		Random r = new Random();
		hascoFactory.setProblemInput(new RefinementConfiguredSoftwareConfigurationProblem<>(new File("testrsc/difficultproblem.json"), "IFace", n -> r.nextDouble()));
		HASCOViaFDAndBestFirst<Double> hasco = hascoFactory.getAlgorithm();
		hasco.setNumCPUs(1);
		
		new JFXPanel();
		Platform.runLater(new AlgorithmVisualizationWindow(hasco, new GraphViewPlugin(), new SolutionPerformanceTimelinePlugin()));
		hasco.call();
	}
}
