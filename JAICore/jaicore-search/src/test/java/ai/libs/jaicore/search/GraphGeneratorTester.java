package ai.libs.jaicore.search;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;
import org.junit.Test;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.graph.LabeledGraph;

public abstract class GraphGeneratorTester<N, A> {

	public abstract List<Pair<IGraphGenerator<N, A>,Integer>> getGraphGenerators() throws Exception;

	private class Node {
		N point;
		int depth;
		public Node(final N point, final int depth) {
			super();
			this.point = point;
			this.depth = depth;
		}
	}

	@Test
	public void testIdempotency() throws Exception {
		for (Pair<IGraphGenerator<N, A>, Integer> pair : this.getGraphGenerators()) {
			IGraphGenerator<N, A> g = pair.getX();
			int maxN = pair.getY();
			ISuccessorGenerator<N, A> s = g.getSuccessorGenerator();

			int n1 = 0;
			int n2 = 0;
			LabeledGraph<N, A> g1 = new LabeledGraph<>();
			LabeledGraph<N, A> g2 = new LabeledGraph<>();
			Queue<Node> open = new LinkedList<>();
			int maxDepth;

			/* run bfs from left */
			N root1 = ((ISingleRootGenerator<N>) g.getRootGenerator()).getRoot();
			open.clear();
			open.add(new Node(root1, 0));
			g1.addItem(root1);
			maxDepth = Integer.MAX_VALUE;
			while (!open.isEmpty()) {
				n1 ++;
				Node expandedNode = open.poll();
				if (expandedNode.depth > maxDepth) {
					break;
				}
				List<INewNodeDescription<N, A>> successors = s.generateSuccessors(expandedNode.point);
				if (maxDepth == Integer.MAX_VALUE && n1 >= maxN) {
					maxDepth = expandedNode.depth;
				}
				successors.forEach(nd -> {
					open.add(new Node(nd.getTo(), expandedNode.depth + 1));
					g1.addItem(nd.getTo());
					g1.addEdge(expandedNode.point, nd.getTo(), nd.getArcLabel());
				});
			}

			/* run bfs from right */
			N root2 = ((ISingleRootGenerator<N>) g.getRootGenerator()).getRoot();
			open.clear();
			open.add(new Node(root2, 0));
			g2.addItem(root2);
			maxDepth = Integer.MAX_VALUE;
			while (!open.isEmpty()) {
				n2 ++;
				Node expandedNode = open.poll();
				if (expandedNode.depth > maxDepth) {
					break;
				}
				List<INewNodeDescription<N, A>> successors = s.generateSuccessors(expandedNode.point);
				Collections.reverse(successors);
				if (maxDepth == Integer.MAX_VALUE && n2 >= maxN) {
					maxDepth = expandedNode.depth;
				}
				successors.forEach(nd -> {
					open.add(new Node(nd.getTo(), expandedNode.depth + 1));
					g2.addItem(nd.getTo());
					g2.addEdge(expandedNode.point, nd.getTo(), nd.getArcLabel());
				});
			}

			assertEquals(n1, n2);
			assertEquals(g1, g2);
		}
	}

}
