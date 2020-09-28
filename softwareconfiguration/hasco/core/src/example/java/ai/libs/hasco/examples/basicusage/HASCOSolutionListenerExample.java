package ai.libs.hasco.examples.basicusage;

import java.io.File;
import java.io.IOException;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFD;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;

/**
 * @author Felix Mohr
 */
public class HASCOSolutionListenerExample {
	public static void main(final String[] args) throws IOException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {

		/* create algorithm */
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(new File("../../../JAICore/jaicore-components/testrsc/simpleproblem.json"), "IFace", n -> 0.0);
		HASCOViaFD<Double> hasco = HASCOBuilder.get().withProblem(problem).withDFS().getAlgorithm();

		/* register listener */
		hasco.registerSolutionEventListener(e -> System.out.println("Received solution with score " + e.getScore() + ": " + e.getSolutionCandidate().getComponentInstance()));

		/* find all solutions */
		hasco.call();
	}
}
