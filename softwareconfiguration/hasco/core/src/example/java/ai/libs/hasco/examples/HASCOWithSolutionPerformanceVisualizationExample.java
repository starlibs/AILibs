package ai.libs.hasco.examples;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;

import ai.libs.hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.hasco.gui.statsplugin.HASCOSolutionCandidateRepresenter;
import ai.libs.hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirst;
import ai.libs.hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstFactory;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionPerformanceTimelinePlugin;
import ai.libs.jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;

public class HASCOWithSolutionPerformanceVisualizationExample {
	public static void main(final String[] args) throws IOException, InterruptedException, TimeoutException, AlgorithmException, AlgorithmExecutionCanceledException {
		HASCOViaFDAndBestFirstFactory<Double> hascoFactory = new HASCOViaFDAndBestFirstFactory<>(n -> 0.0);
		Random r = new Random();
		hascoFactory.setProblemInput(new RefinementConfiguredSoftwareConfigurationProblem<>(new File("testrsc/difficultproblem.json"), "IFace", n -> r.nextDouble()));
		HASCOViaFDAndBestFirst<Double> hasco = hascoFactory.getAlgorithm();
		hasco.setNumCPUs(1);

		AlgorithmVisualizationWindow window = new AlgorithmVisualizationWindow(hasco);
		window.withMainPlugin(new GraphViewPlugin());
		window.withPlugin(new SolutionPerformanceTimelinePlugin(new HASCOSolutionCandidateRepresenter()));
		hasco.call();
	}
}
