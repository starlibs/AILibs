package jaicore.search.algorithms.standard.bestfirst.abstractVersioning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import jaicore.search.algorithms.standard.ORGraphSearchTester;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.graphgenerators.bestfirst.abstractVersioning.TestGraphGenerator;
import jaicore.search.graphgenerators.bestfirst.abstractVersioning.TestNode;
import jaicore.search.structure.core.NodeExpansionDescription;

public class AbstractVersioningTester extends ORGraphSearchTester {

	@Test
	public void testSequential() throws InterruptedException {
		TestGraphGenerator gen = new TestGraphGenerator();

		BestFirst<TestNode, String> bf = new BestFirst<>(gen, n -> (double) Math.round(Math.random() * 100));

		// new SimpleGraphVisualizationWindow<>(bf.getEventBus()).getPanel().setTooltipGenerator(n-> String.valueOf(n.getInternalLabel()));

		// set node numbering to false
		gen.setNodeNumbering(false);

		/*find the solution*/
		List<TestNode> solutionPath = bf.nextSolution();
		solutionPath.stream().forEach(n -> {
			assertEquals(n.getId(), -1);
		});

		/*second test now with numbering.
		 */
		gen.reset();
		bf = new BestFirst<>(gen, n -> (double) Math.round(Math.random() * 100));
		// new SimpleGraphVisualizationWindow<>(bf.getEventBus()).getPanel().setTooltipGenerator(n-> String.valueOf(n.getInternalLabel()));
		gen.setNodeNumbering(true);
		List<TestNode> solutionPath2 = bf.nextSolution();
		Set<Integer> ids = new HashSet<Integer>();

		solutionPath2.stream().forEach(n -> {
			assertTrue(n.getId() > 0);
			assertFalse(ids.contains(n.getId()));

			ids.add(n.getId());
		});

	}

	@Test
	public void testParallelized() throws InterruptedException {
		TestGraphGenerator gen = new TestGraphGenerator();
		gen.setNodeNumbering(true);

		BestFirst<TestNode, String> bf = new BestFirst<>(gen, n -> (double) Math.round(Math.random() * 100));
		bf.parallelizeNodeExpansion(2);
		bf.setTimeoutForComputationOfF(350, node -> 100.0);

		List<TestNode> solutionPath2 = bf.nextSolution();
		Set<Integer> ids = new HashSet<Integer>();

		solutionPath2.stream().forEach(n -> {
			assertTrue(n.getId() > 0);
			assertFalse(ids.contains(n.getId()));

			ids.add(n.getId());
		});

	}

	@Test
	public void testIterable() {
		TestGraphGenerator gen = new TestGraphGenerator();
		gen.setNodeNumbering(true);

		Set<Integer> ids = new HashSet<Integer>();

		BestFirst<TestNode, String> bf = new BestFirst<>(gen, n -> (double) Math.round(Math.random() * 100));

		/*find the solution*/
		List<TestNode> solutionPath = null;

		while (solutionPath == null) {
			List<NodeExpansionDescription<TestNode, String>> expansion = bf.nextExpansion();
			for (NodeExpansionDescription des : expansion) {
				if (ids.contains(((TestNode) des.getTo()).getId())) {
					fail();
				} else
					ids.add(((TestNode) des.getTo()).getId());
			}
			assertNotNull(expansion);
		}

	}

}
