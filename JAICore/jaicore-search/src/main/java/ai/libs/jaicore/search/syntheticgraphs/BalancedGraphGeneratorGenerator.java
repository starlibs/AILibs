package ai.libs.jaicore.search.syntheticgraphs;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.NodeExpansionDescription;
import org.api4.java.datastructure.graph.implicit.NodeType;
import org.api4.java.datastructure.graph.implicit.SingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.SuccessorGenerator;

public class BalancedGraphGeneratorGenerator {

	private final int branchingFactor;
	private final int depth;

	class N {
		int depth;
		int idOfNodeOnLayer;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.depth;
			result = prime * result + this.idOfNodeOnLayer;
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
			public SuccessorGenerator<N, Integer> getSuccessorGenerator() {
				return new SuccessorGenerator<N, Integer>() {

					@Override
					public List<NodeExpansionDescription<N, Integer>> generateSuccessors(final N node) throws InterruptedException {
						List<NodeExpansionDescription<N, Integer>> successors = new ArrayList<>();
						int d = node.depth + 1;
						int offsetForIdOnLayer = BalancedGraphGeneratorGenerator.this.branchingFactor * node.idOfNodeOnLayer;
						if (d > BalancedGraphGeneratorGenerator.this.depth) {
							return successors;
						}
						for (int i = 0; i < BalancedGraphGeneratorGenerator.this.branchingFactor; i++) {
							N successor = new N();
							successor.depth = d;
							successor.idOfNodeOnLayer = offsetForIdOnLayer + i;
							successors.add(new NodeExpansionDescription<>(successor, i, NodeType.OR));
						}
						return successors;
					}
				};
			}
		};
	}
}
