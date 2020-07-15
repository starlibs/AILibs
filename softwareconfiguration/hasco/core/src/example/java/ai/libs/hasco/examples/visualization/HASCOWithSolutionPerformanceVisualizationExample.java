package ai.libs.hasco.examples.visualization;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFD;
import ai.libs.hasco.gui.statsplugin.HASCOSolutionCandidateRepresenter;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionPerformanceTimelinePlugin;
import ai.libs.jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;

public class HASCOWithSolutionPerformanceVisualizationExample {

	public static final String pathToExamples = "../../../JAICore/jaicore-components/";

	public static void main(final String[] args) throws IOException, InterruptedException, TimeoutException, AlgorithmException, AlgorithmExecutionCanceledException {
		Random r = new Random();
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(new File(pathToExamples +"testrsc/difficultproblem.json"), "IFace", n -> r.nextDouble());
		HASCOViaFD<Double> hasco = HASCOBuilder.get(problem).withBlindSearch().getAlgorithm();
		hasco.setNumCPUs(1);

		AlgorithmVisualizationWindow window = new AlgorithmVisualizationWindow(hasco);
		window.withMainPlugin(new GraphViewPlugin());
		window.withPlugin(new SolutionPerformanceTimelinePlugin(new HASCOSolutionCandidateRepresenter()));
		hasco.call();
	}
}
