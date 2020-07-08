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
 *
 */
public class HASCOBasicUsageExample4DFS {

	public static void main(final String[] args) throws IOException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(new File("../../../JAICore/jaicore-components/testrsc/difficultproblem.json"), "IFace", n -> 0.0);
		HASCOViaFD<Double> hasco = HASCOBuilder.get().withProblem(problem).withDFS().getAlgorithm();
		hasco.nextSolutionCandidate().getScore();
	}
}
