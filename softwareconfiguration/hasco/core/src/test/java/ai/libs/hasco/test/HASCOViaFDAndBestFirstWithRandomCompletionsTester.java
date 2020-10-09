package ai.libs.hasco.test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;

import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFD;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFDAndBestFirstWithRandomCompletionsBuilder;
import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.hasco.core.HASCOUtil;
import ai.libs.hasco.core.events.HASCOSolutionEvent;
import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.components.model.ComponentInstanceUtil;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class HASCOViaFDAndBestFirstWithRandomCompletionsTester extends HASCOTester<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, Double>, TFDNode, String> {

	public static HASCOViaFDAndBestFirstWithRandomCompletionsBuilder getBuilder() {
		return HASCOBuilder.get().withBestFirst().withRandomCompletions();
	}

	@Override
	public HASCO<TFDNode, String, Double> getAlgorithmForSoftwareConfigurationProblem(final RefinementConfiguredSoftwareConfigurationProblem<Double> problem) {
		return getBuilder().withProblem(problem).getAlgorithm();
	}

	@ParameterizedTest(name = "Repository: {0}")
	@MethodSource("getAllCompositionProblems")
	public void testThatDefaultParametrizationsAreEvaluatedFirst(final String name, final RefinementConfiguredSoftwareConfigurationProblem<Double> problem)
			throws AlgorithmTestProblemSetCreationException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {

		int expectedDefaultConfigurations = HASCOUtil.getNumberOfUnparametrizedSolutions(problem);
		HASCOViaFD<Double> hasco = getBuilder().withProblem(problem).withDefaultParametrizationsFirst().withNumSamples(expectedDefaultConfigurations).getAlgorithm();
		hasco.setLoggerName("testedalgorithm");
		hasco.setTimeout(new Timeout(30, TimeUnit.SECONDS));

		boolean haveSeenNonDefault = false;
		int seenDefaultConfigurations = 0;
		this.logger.info("Testing on problem with {} components. Expecting {} default configurations.", problem.getComponents().size(), expectedDefaultConfigurations);

		ComponentSerialization serializer = new ComponentSerialization();
		while (hasco.hasNext()) {
			try {
				IAlgorithmEvent e = hasco.nextWithException();

				if (e instanceof HASCOSolutionEvent) {
					@SuppressWarnings("unchecked")
					HASCOSolutionCandidate<Double> s = ((HASCOSolutionEvent<Double>) e).getSolutionCandidate();
					boolean isDefault = ComponentInstanceUtil.isDefaultConfiguration(s.getComponentInstance());
					assertTrue("Found default configured component instance after others that were not default-configured.", !haveSeenNonDefault || !isDefault);
					assertTrue("Have not seen all expected default configuration prior to a non-default component instance.\nExpected: " + expectedDefaultConfigurations + "\nSeen: " + seenDefaultConfigurations
							+ "\nCurrent component instance: " + s.getComponentInstance(), isDefault || seenDefaultConfigurations == expectedDefaultConfigurations);
					this.logger.info("Observed solution {}", serializer.serialize(s.getComponentInstance()));
					if (isDefault) {
						seenDefaultConfigurations++;
					} else {
						haveSeenNonDefault = true;
					}

					if (seenDefaultConfigurations == expectedDefaultConfigurations) {
						hasco.cancel();
					}
				}
			} catch (AlgorithmTimeoutedException ex) {
				/* break */
				this.logger.info("Received timeout without having observed any problem; passing to next test.");
				break;
			}
		}
		assertEquals(expectedDefaultConfigurations, seenDefaultConfigurations);
	}
}
