package ai.libs.jaicore.search.algorithms.standard.bestfirst;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;

import ai.libs.jaicore.problems.npuzzle.NPuzzleProblem;
import ai.libs.jaicore.problems.npuzzle.NPuzzleProblemGenerator;
import ai.libs.jaicore.search.exampleproblems.npuzzle.standard.NPuzzleGraphGenerator;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

/**
 * This test captures the BestFirst algorithm's ability to traverse search graphs that are not trees.
 *
 * @author Felix Mohr
 *
 */
public class GraphSearchTest {

	@Test
	public void test() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		NPuzzleProblem prob = new NPuzzleProblemGenerator().generate(3);
		NPuzzleGraphGenerator gg = new NPuzzleGraphGenerator(prob.getBoard());
		GraphSearchWithSubpathEvaluationsInput<NPuzzleProblem, String, Double> searchprob = new GraphSearchWithSubpathEvaluationsInput<>(gg, p -> p.getHead().getNumberOfWrongTiles() == 0, p -> p.getNumberOfNodes() * 1.0);
		StandardBestFirst<NPuzzleProblem, String, Double> bf = new StandardBestFirst<>(searchprob);
		System.out.println(bf.call());
	}

}
