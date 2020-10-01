package ai.libs.jaicore.basic.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ai.libs.jaicore.graph.Graph;
import ai.libs.jaicore.test.ShortTest;

public class GraphConstructionTest {

	@ShortTest
	public void testThatNewNodesOccurInNodeSet() {
		String node = "a";
		Graph<String> g = new Graph<>();
		assertFalse(g.getItems().contains(node));
		g.addItem(node);
		assertTrue(g.getItems().contains(node));
	}

	@ShortTest
	public void testThatRemovedNodesAreNotInNodeSet() {
		String node = "a";
		Graph<String> g = new Graph<>();
		g.addItem(node);
		assertTrue(g.getItems().contains(node));
		g.removeItem(node);
		assertFalse(g.getItems().contains(node));
	}

	@ShortTest
	public void testThatAddingEdgesImpliesAddingNodes() {
		String node1 = "a";
		String node2 = "b";
		Graph<String> g = new Graph<>();
		assertFalse(g.getItems().contains(node1));
		assertFalse(g.getItems().contains(node2));
		g.addEdge(node1, node2);
		assertTrue(g.getItems().contains(node1));
		assertTrue(g.getItems().contains(node2));
		assertEquals(1, g.getSuccessors(node1).size());
		assertTrue(g.getSuccessors(node1).contains(node2));
		assertEquals(1, g.getPredecessors(node2).size());
		assertTrue(g.getPredecessors(node2).contains(node1));
	}

	@ShortTest
	public void testThatNodeRemovalImpliesEdgeRemoval() {
		String node1 = "a";
		String node2 = "b";
		Graph<String> g = new Graph<>();
		g.addEdge(node1, node2);
		assertEquals(1, g.getPredecessors(node2).size());
		g.removeItem(node1);
		assertEquals(0, g.getPredecessors(node2).size());
	}

	@ShortTest
	public void testThatNewNodesAreSources() {
		String node1 = "a";
		Graph<String> g = new Graph<>();
		g.addItem(node1);
		assertTrue(g.getSources().contains(node1));
	}

	@ShortTest
	public void testThatNewNodesAreSinks() {
		String node1 = "a";
		Graph<String> g = new Graph<>();
		g.addItem(node1);
		assertTrue(g.getSinks().contains(node1));
	}
}
