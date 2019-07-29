package ai.libs.jaicore.search.syntheticgraphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.NodeExpansionDescription;
import org.api4.java.datastructure.graph.implicit.NodeType;
import org.api4.java.datastructure.graph.implicit.SingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.SingleSuccessorGenerator;

public class BalancedGraphGeneratorGenerator {

	private final int branchingFactor;
	private final int depth;

	class N {
		int depth;
		long idOfNodeOnLayer;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.depth;
			result = prime * result + (int)this.idOfNodeOnLayer;
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (this.getClass() != obj.getClass()) {
				return false;
			}
			N other = (N) obj;
			if (this.depth != other.depth) {
				return false;
			}
			if (this.idOfNodeOnLayer != other.idOfNodeOnLayer) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "N [depth=" + this.depth + ", idOfNodeOnLayer=" + this.idOfNodeOnLayer + "]";
		}
	}

	public BalancedGraphGeneratorGenerator(final int branchingFactor, final int depth) {
		super();
		this.branchingFactor = branchingFactor;
		this.depth = depth;
	}

	public IGraphGenerator<N, Integer> create() {
		return new IGraphGenerator<N, Integer>() {

			@Override
			public SingleRootGenerator<N> getRootGenerator() {
				return N::new;
			}

			@Override
			public SingleSuccessorGenerator<N, Integer> getSuccessorGenerator() {
				return new SingleSuccessorGenerator<N, Integer>() {

					private Map<N, Set<Integer>> successors = new HashMap<>();

					@Override
					public List<NodeExpansionDescription<N, Integer>> generateSuccessors(final N node) throws InterruptedException {
						List<NodeExpansionDescription<N, Integer>> successorsOfThisNode = new ArrayList<>();
						int d = node.depth + 1;
						if (d > BalancedGraphGeneratorGenerator.this.depth) {
							return successorsOfThisNode;
						}
						for (int i = 0; i < BalancedGraphGeneratorGenerator.this.branchingFactor; i++) {
							successorsOfThisNode.add(this.generateSuccessor(node, i));
						}
						return successorsOfThisNode;
					}

					@Override
					public NodeExpansionDescription<N, Integer> generateSuccessor(final N node, final int i) throws InterruptedException {
						int j = i % BalancedGraphGeneratorGenerator.this.branchingFactor;
						int d = node.depth + 1;
						long offsetForIdOnLayer = BalancedGraphGeneratorGenerator.this.branchingFactor * node.idOfNodeOnLayer;
						N successor = new N();
						successor.depth = d;
						successor.idOfNodeOnLayer = offsetForIdOnLayer + j;
						this.successors.computeIfAbsent(node, n -> new HashSet<>()).add(i);
						return new NodeExpansionDescription<>(successor, j, NodeType.OR);
					}

					@Override
					public boolean allSuccessorsComputed(final N node) {
						return this.successors.get(node) != null && this.successors.get(node).size() == BalancedGraphGeneratorGenerator.this.branchingFactor;
					}
				};
			}
		};
	}
}
