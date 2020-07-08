package ai.libs.hasco.examples.visualization;

import java.io.File;
import java.io.IOException;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFD;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import ai.libs.jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;

public class HASCOSearchVisualizationExample {
	public static void main(final String[] args) throws IOException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {

		/* create algorithm */
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(new File("../../../JAICore/jaicore-components/testrsc/difficultproblem.json"), "IFace", n -> 0.0);
		HASCOViaFD<Double> hasco = HASCOBuilder.get().withProblem(problem).withDFS().getAlgorithm();

		/* create algorithm visualizer */
		new AlgorithmVisualizationWindow(hasco).withMainPlugin(new GraphViewPlugin());

		/* find all solutions*/
		hasco.call();
	}
}
