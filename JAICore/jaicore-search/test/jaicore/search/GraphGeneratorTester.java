package jaicore.search;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.junit.Test;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.graph.LabeledGraph;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public abstract class GraphGeneratorTester<N, A> {

	public abstract List<Pair<GraphGenerator<N, A>,Integer>> getGraphGenerators() throws Exception;

	private class Node {
		N point;
		int depth;
		public Node(N point, int depth) {
			super();
			this.point = point;
			this.depth = depth;
		}
	}
	
	@Test
	public void testIdempotency() throws Exception {
		for (Pair<GraphGenerator<N, A>, Integer> pair : getGraphGenerators()) {
			GraphGenerator<N, A> g = pair.getX();
			int maxN = pair.getY();
			SuccessorGenerator<N, A> s = g.getSuccessorGenerator();

			int n1 = 0;
			int n2 = 0;
			LabeledGraph<N, A> g1 = new LabeledGraph<>();
			LabeledGraph<N, A> g2 = new LabeledGraph<>();
			Queue<Node> open = new LinkedList<>();
			int maxDepth;

			/* run bfs from left */
			N root1 = ((SingleRootGenerator<N>) g.getRootGenerator()).getRoot();
			open.clear();
			open.add(new Node(root1, 0));
			g1.addItem(root1);
			maxDepth = Integer.MAX_VALUE;
			while (!open.isEmpty()) {
				n1 ++;
//				System.out.println("G1: Expanding " + n1 + "th node");
				Node expandedNode = open.poll();
				if (expandedNode.depth > maxDepth)
					break;
				List<NodeExpansionDescription<N, A>> successors = s.generateSuccessors(expandedNode.point);
				if (maxDepth == Integer.MAX_VALUE && n1 >= maxN) {
					maxDepth = expandedNode.depth;
				}
				successors.forEach(nd -> {
					open.add(new Node(nd.getTo(), expandedNode.depth + 1));
					g1.addItem(nd.getTo());
					g1.addEdge(nd.getFrom(), nd.getTo(), nd.getAction());
				});
			}
			
			/* run bfs from right */
			N root2 = ((SingleRootGenerator<N>) g.getRootGenerator()).getRoot();
			open.clear();
			open.add(new Node(root2, 0));
			g2.addItem(root2);
			maxDepth = Integer.MAX_VALUE;
			while (!open.isEmpty()) {
				n2 ++;
//				System.out.println("G2: Expanding " + n2 + "th node");
				Node expandedNode = open.poll();
				if (expandedNode.depth > maxDepth)
					break;
				List<NodeExpansionDescription<N, A>> successors = s.generateSuccessors(expandedNode.point);
				Collections.reverse(successors);
				if (maxDepth == Integer.MAX_VALUE && n2 >= maxN) {
					maxDepth = expandedNode.depth;
				}
				successors.forEach(nd -> {
					open.add(new Node(nd.getTo(), expandedNode.depth + 1));
					g2.addItem(nd.getTo());
					g2.addEdge(nd.getFrom(), nd.getTo(), nd.getAction());
				});
			}
			
			assertEquals(n1, n2);
			assertEquals(g1, g2);
		}
	}

}
