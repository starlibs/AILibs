package ai.libs.hasco.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFD;
import ai.libs.hasco.core.events.HASCOSolutionEvent;
import ai.libs.jaicore.basic.Tester;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.logging.LoggerUtil;

public class ListBasedRequiredInterfaceSolutionProductionTester extends Tester {

	private static final File folderToTestResources = new File("../../../JAICore/jaicore-components/testrsc/");
	private static final String reqInterface = "IFace";
	private static final File tinyProblem = new File(folderToTestResources + File.separator + "tinyproblemwithlists.json");
	private static final File tinyProblemWithUniqueness = new File(folderToTestResources + File.separator + "tinyproblemwithuniquelists.json");
	private static final File tinyProblemWithOptional = new File(folderToTestResources + File.separator + "tinyproblemwithoptionallists.json");
	private static final File smallProblem = new File(folderToTestResources + File.separator + "simpleproblemwithlists.json");

	@Test
	public void testCorrectNumberOfSolutionsInListInterfacesOnTinyProblem() throws IOException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		this.test(tinyProblem, 3);
	}

	@Test
	public void testCorrectNumberOfSolutionsInUniqueListInterfacesOnTinyProblem() throws IOException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		this.test(tinyProblemWithUniqueness, 2);
	}

	@Test
	public void testCorrectNumberOfSolutionsInOptionalListInterfacesOnTinyProblem() throws IOException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		this.test(tinyProblemWithOptional, 4);
	}

	@Test
	public void testCorrectNumberOfSolutionsInListInterfacesOnSmallProblem() throws IOException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		this.test(smallProblem, 378);
	}

	public void test(final File file, final int expectedSolutions) throws IOException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {

		/* load original software configuration problem */
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(file, reqInterface, n -> 0.0);
		HASCOViaFD<Double> hasco = HASCOBuilder.get(problem).withBlindSearch().getAlgorithm();
		hasco.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);

		//		new AlgorithmVisualizationWindow(hasco).withMainPlugin(new GraphViewPlugin()).withPlugin(new NodeInfoGUIPlugin(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())));

		/* enumerate all solutions */
		Set<String> seenSolutions = new HashSet<>();
		while (hasco.hasNext()) {
			IAlgorithmEvent event = hasco.nextWithException();
			if (event instanceof HASCOSolutionEvent) {
				@SuppressWarnings("unchecked")
				ComponentInstance solution = ((HASCOSolutionEvent<Double>) event).getSolutionCandidate().getComponentInstance();
				String serializedSolution = new ComponentSerialization().serialize(solution).toString();
				assertFalse("Double solution " + serializedSolution, seenSolutions.contains(serializedSolution));
				seenSolutions.add(serializedSolution);
				LOGGER.info("Registered {}-th solution: {}", seenSolutions.size(), serializedSolution);
			}
		}
		assertEquals(expectedSolutions, seenSolutions.size());
	}
}
