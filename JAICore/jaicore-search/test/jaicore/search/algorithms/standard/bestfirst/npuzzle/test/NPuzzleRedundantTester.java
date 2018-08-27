package jaicore.search.algorithms.standard.bestfirst.npuzzle.test;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.graphgenerators.npuzzle.redundant.NPuzzleRedundantGenerator;
import jaicore.search.graphgenerators.npuzzle.standard.NPuzzleNode;

public class NPuzzleRedundantTester {

	@Test
	public void test() throws InterruptedException {
		NPuzzleRedundantGenerator gen = new NPuzzleRedundantGenerator(3, 4);
		BestFirst<NPuzzleNode, String> search = new BestFirst<>(gen, n -> (double) n.getPoint().getNumberOfWrongTiles());

		// SimpleGraphVisualizationWindow<Node<NPuzzleNode,Double>> win = new SimpleGraphVisualizationWindow<>(search.getEventBus());
		// win.getPanel().setTooltipGenerator(n->n.getPoint().toString());

		/*search for solution*/
		List<NPuzzleNode> solutionPath = search.nextSolution();
		assertNotNull(solutionPath);
	}

}
