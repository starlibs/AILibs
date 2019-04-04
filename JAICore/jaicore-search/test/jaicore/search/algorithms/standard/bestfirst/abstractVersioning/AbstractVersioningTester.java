//package jaicore.search.algorithms.standard.bestfirst.abstractVersioning;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import org.junit.Test;
//
//import jaicore.graph.IGraphAlgorithmListener;
//import jaicore.search.algorithms.EvaluatedSearchAlgorithmSolution;
//import jaicore.search.algorithms.interfaces.IORGraphSearch;
//import jaicore.search.algorithms.interfaces.IORGraphSearchFactory;
//import jaicore.search.algorithms.standard.ORGraphSearchTester;
//import jaicore.search.algorithms.standard.bestfirst.BestFirst;
//import jaicore.search.algorithms.standard.bestfirst.model.NodeExpansionDescription;
//import jaicore.search.testproblems.bestfirst.abstractVersioning.TestGraphGenerator;
//import jaicore.search.testproblems.bestfirst.abstractVersioning.TestNode;
//import jaicore.search.testproblems.nqueens.QueenNode;
//
//public class AbstractVersioningTester<O> extends ORGraphSearchTester<TestNode, String, Double> {
//
//	public void testSequential(IORGraphSearchFactory<V, E, IGraphAlgorithmListener<V, E>, O> factory) throws InterruptedException {
//		TestGraphGenerator gen = new TestGraphGenerator();
//
//		BestFirst<TestNode, String, Double> bf = new BestFirst<>(gen, n -> (double) Math.round(Math.random() * 100));
//
//		// new SimpleGraphVisualizationWindow<>(bf.getEventBus()).getPanel().setTooltipGenerator(n-> String.valueOf(n.getInternalLabel()));
//
//		// set node numbering to false
//		gen.setNodeNumbering(false);
//
//		/*find the solution*/
//		EvaluatedSearchAlgorithmSolution<TestNode, String, Double> solution = bf.nextSolution();
//		List<TestNode> solutionPath = solution.getNodes();
//		solutionPath.stream().forEach(n -> {
//			assertEquals(n.getId(), -1);
//		});
//
//		/*second test now with numbering.
//		 */
//		gen.reset();
//		bf = new BestFirst<>(gen, n -> (double) Math.round(Math.random() * 100));
//		// new SimpleGraphVisualizationWindow<>(bf.getEventBus()).getPanel().setTooltipGenerator(n-> String.valueOf(n.getInternalLabel()));
//		gen.setNodeNumbering(true);
//		EvaluatedSearchAlgorithmSolution<TestNode, String, Double> solution2 = bf.nextSolution();
//		List<TestNode> solutionPath2 = solution2.getNodes();
//		Set<Integer> ids = new HashSet<Integer>();
//
//		solutionPath2.stream().forEach(n -> {
//			assertTrue(n.getId() > 0);
//			assertFalse(ids.contains(n.getId()));
//
//			ids.add(n.getId());
//		});
//
//	}
//
//	public void testParallelized(IORGraphSearchFactory<V, E, IGraphAlgorithmListener<V, E>, O> factory) throws InterruptedException {
//		TestGraphGenerator gen = new TestGraphGenerator();
//		gen.setNodeNumbering(true);
//
//		BestFirst<TestNode, String, Double> bf = new BestFirst<>(gen, n -> (double) Math.round(Math.random() * 100));
//		bf.parallelizeNodeExpansion(2);
//		bf.setTimeoutForComputationOfF(350, node -> 100.0);
//
//		EvaluatedSearchAlgorithmSolution<TestNode, String, Double> solution2 = bf.nextSolution();
//		List<TestNode> solutionPath2 = solution2.getNodes();
//		Set<Integer> ids = new HashSet<Integer>();
//
//		solutionPath2.stream().forEach(n -> {
//			assertTrue(n.getId() > 0);
//			assertFalse(ids.contains(n.getId()));
//
//			ids.add(n.getId());
//		});
//
//	}
//
//	public void testIterable(IORGraphSearchFactory<TestNode, String, IGraphAlgorithmListener<TestNode, String>, Double> factory) {
//		TestGraphGenerator gen = new TestGraphGenerator();
//		gen.setNodeNumbering(true);
//
//		Set<Integer> ids = new HashSet<Integer>();
//
//		IORGraphSearch<TestNode, String, ?, ?> bf = factory.getAlgorithm();
//
//		/*find the solution*/
//		List<TestNode> solutionPath = null;
//
//		while (solutionPath == null) {
//			List<NodeExpansionDescription<TestNode, String>> expansion = bf.nextExpansion();
//			for (NodeExpansionDescription des : expansion) {
//				if (ids.contains(((TestNode) des.getTo()).getId())) {
//					fail();
//				} else
//					ids.add(((TestNode) des.getTo()).getId());
//			}
//			assertNotNull(expansion);
//		}
//
//	}
//
//}
