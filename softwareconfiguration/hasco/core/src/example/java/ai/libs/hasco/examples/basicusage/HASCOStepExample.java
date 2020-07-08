package ai.libs.hasco.examples.basicusage;

import java.io.File;
import java.io.IOException;

import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFD;
import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.hasco.core.events.HASCOSolutionEvent;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;

public class HASCOStepExample {
	public static void main(final String[] args) throws IOException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {

		/* create algorithm */
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(new File("../../../JAICore/jaicore-components/testrsc/simpleproblem.json"), "IFace", n -> 0.0);
		HASCOViaFD<Double> hasco = HASCOBuilder.get().withProblem(problem).withDFS().getAlgorithm();

		/* step over events until algorithm finishes */
		while (hasco.hasNext()) {
			IAlgorithmEvent event = hasco.nextWithException();
			if (event instanceof HASCOSolutionEvent) {
				@SuppressWarnings("unchecked")
				HASCOSolutionCandidate<Double> s = ((HASCOSolutionEvent<Double>) event).getSolutionCandidate();
				System.out.println("Received solution with score " + s.getScore() + ": " + s.getComponentInstance());
			}
		}
	}
}
