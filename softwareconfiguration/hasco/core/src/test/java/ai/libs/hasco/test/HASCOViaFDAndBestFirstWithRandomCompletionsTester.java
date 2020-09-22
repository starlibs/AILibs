package ai.libs.hasco.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFD;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFDAndBestFirstWithRandomCompletionsBuilder;
import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.hasco.core.events.HASCOSolutionEvent;
import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.components.model.ComponentInstanceUtil;
import ai.libs.jaicore.components.model.ComponentUtil;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class HASCOViaFDAndBestFirstWithRandomCompletionsTester extends HASCOTester<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, Double>, TFDNode, String> {

	private Logger logger = LoggerFactory.getLogger("tester");

	public static HASCOViaFDAndBestFirstWithRandomCompletionsBuilder getBuilder() {
		return HASCOBuilder.get().withBestFirst().viaRandomCompletions();
	}

	@Override
	public HASCO<TFDNode, String, Double> getAlgorithmForSoftwareConfigurationProblem(final RefinementConfiguredSoftwareConfigurationProblem<Double> problem) {
		return getBuilder().withProblem(problem).getAlgorithm();
	}

	@Test
	public void testThatDefaultParametrizationsAreEvaluatedFirst() throws AlgorithmTestProblemSetCreationException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {

		/* setup problems for which we want to check this */
		List<RefinementConfiguredSoftwareConfigurationProblem<Double>> problems = new ArrayList<>();
		problems.add(this.getProblemSet().getSimpleProblemInputForGeneralTestPurposes());
		problems.add(this.getProblemSet().getSimpleProblemInputWithTwoComponents());
		problems.add(this.getProblemSet().getSimpleRecursiveProblemInput());
		problems.add(this.getProblemSet().getDifficultProblemInputForGeneralTestPurposes());

		/* now run checks */
		for (RefinementConfiguredSoftwareConfigurationProblem<Double> problem : problems) {
			int expectedDefaultConfigurations = ComponentUtil.getNumberOfUnparametrizedCompositions(problem.getComponents(), problem.getRequiredInterface());
			HASCOViaFD<Double> hasco = getBuilder().withProblem(problem).withDefaultParametrizationsFirst().withNumSamples(expectedDefaultConfigurations).getAlgorithm();
			hasco.setLoggerName("testedalgorithm");
			hasco.setTimeout(new Timeout(30, TimeUnit.SECONDS));

			boolean haveSeenNonDefault = false;
			int seenDefaultConfigurations = 0;
			this.logger.info("Testing on problem with {} components. Expecting {} default configurations.", problem.getComponents().size(), expectedDefaultConfigurations);
			while (hasco.hasNext()) {
				try {
					IAlgorithmEvent e = hasco.nextWithException();

					if (e instanceof HASCOSolutionEvent) {
						@SuppressWarnings("unchecked")
						HASCOSolutionCandidate<Double> s = ((HASCOSolutionEvent<Double>) e).getSolutionCandidate();
						boolean isDefault = ComponentInstanceUtil.isDefaultConfiguration(s.getComponentInstance());
						assertTrue("Found default configured component instance after others that were not default-configured.", !haveSeenNonDefault || !isDefault);
						assertTrue("Have not seen all expected default configuration prior to a non-default component instance.\nExpected: " + expectedDefaultConfigurations + "\nSeen: " + seenDefaultConfigurations + "\nCurrent component instance: " + s.getComponentInstance(), isDefault || seenDefaultConfigurations == expectedDefaultConfigurations);
						if (isDefault) {
							seenDefaultConfigurations ++;
						}
						else {
							haveSeenNonDefault = true;
						}
					}
				}
				catch (AlgorithmTimeoutedException ex) {
					/* break */
					this.logger.info("Received timeout without having observed any problem; passing to next test.");
					break;
				}
			}
			assertEquals(expectedDefaultConfigurations , seenDefaultConfigurations);
		}
	}
}
