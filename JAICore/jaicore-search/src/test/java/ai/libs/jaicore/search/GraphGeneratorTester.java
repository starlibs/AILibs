package ai.libs.jaicore.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.graph.LabeledGraph;
import ai.libs.jaicore.test.LongTest;
import ai.libs.jaicore.test.MediumTest;

public abstract class GraphGeneratorTester<N, A> extends ATest {

	private class Node {
		N point;
		int depth;

		public Node(final N point, final int depth) {
			super();
			this.point = point;
			this.depth = depth;
		}
	}

	@ParameterizedTest(name = "Test idempotency on {0} with 100 iterations")
	@MethodSource("getGraphGenerators")
	public void testIdempotencyQuick(final String name, final IGraphGenerator<N, A> g) throws Exception {
		this.testIdempotency(g, 100);
	}

	@MediumTest
	@ParameterizedTest(name = "Test idempotency on {0} with 1000 iterations")
	@MethodSource("getGraphGenerators")
	public void testIdempotencyMedium(final String name, final IGraphGenerator<N, A> g) throws Exception {
		this.testIdempotency(g, 1000);
	}

	@LongTest
	@ParameterizedTest(name = "Test idempotency on {0} with 1000 iterations")
	@MethodSource("getGraphGenerators")
	public void testIdempotencyLarge(final String name, final IGraphGenerator<N, A> g) throws Exception {
		this.testIdempotency(g, 10000);
	}

	public void testIdempotency(final IGraphGenerator<N, A> g, final int maxN) throws Exception {
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
			n1++;
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
			n2++;
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
