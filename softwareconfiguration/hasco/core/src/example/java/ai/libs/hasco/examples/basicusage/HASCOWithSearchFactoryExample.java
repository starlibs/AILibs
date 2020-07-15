package ai.libs.hasco.examples.basicusage;

import java.io.File;
import java.io.IOException;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFD;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFDBuilder;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.mdp.mcts.uct.UCTFactory;
import ai.libs.jaicore.search.algorithms.standard.mcts.MCTSPathSearchFactory;

/**
 *
 * @author Felix Mohr
 *
 */
public class HASCOWithSearchFactoryExample {

	public static void main(final String[] args) throws IOException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {

		/* create algorithm */
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(new File("../../../JAICore/jaicore-components/testrsc/simpleproblem.json"), "IFace", n -> 0.0);

		/* configure MCTS path search factory */
		MCTSPathSearchFactory<TFDNode, String> mctsPathSearchFactory = new MCTSPathSearchFactory<>();
		UCTFactory<TFDNode, String> uctFactory = new UCTFactory<>();
		mctsPathSearchFactory.withMCTSFactory(uctFactory);

		/* configure the builder with this factory */
		HASCOViaFDBuilder<Double, ?> builder = HASCOBuilder.get(problem);
		builder.withSearchFactory(mctsPathSearchFactory);
		HASCOViaFD<Double> hasco = builder.getAlgorithm();

		/* register listener */
		hasco.registerSolutionEventListener(e -> System.out.println("Received solution with score " + e.getScore() + ": " + e.getSolutionCandidate().getComponentInstance()));

		/* find all solutions */
		for (int i = 0; i < 10; i++) {
			hasco.nextWithException(); // UCT draws the same paths multiple times, so we see solutions several times here
		}
	}
}
