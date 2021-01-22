package ai.libs.hasco.examples.basicusage;

import java.io.File;
import java.io.IOException;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFD;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.components.serialization.ComponentSerialization;

/**
 * @author Felix Mohr
 */
public class HASCOSolutionListenerExample {
	public static void main(final String[] args) throws IOException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {

		final ComponentSerialization serializer = new ComponentSerialization();

		/* create algorithm */
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(
				new File("../../../JAICore/jaicore-components/testrsc/simpleproblem.json"),
				"IFace",
				ci -> {
					System.out.println("Evaluating " + serializer.serialize(ci) + " of type " + ci.getClass());

					String dbName = ci.getComponent().getName();
					System.out.println("Component is " + dbName);

					if (dbName.equals("H2Database")) {
						return 1.0;
					}
					else {
						return 20.0;
					}
				}
				);
		HASCOViaFD<Double> hasco = HASCOBuilder.get().withProblem(problem).withDFS().getAlgorithm();

		/* register listener */
		hasco.registerSolutionEventListener(e -> System.out.println("Received solution with score " + e.getScore() + ": " + serializer.serialize(e.getSolutionCandidate().getComponentInstance())));

		/* find all solutions */
		hasco.call();
	}
}
