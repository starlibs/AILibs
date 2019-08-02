package ai.libs.jaicore.search.syntheticgraphs.graphmodels.balanced;

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

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;

class BalancedGraphGeneratorGenerator {

	private final int branchingFactor;
	private final int maxDepth;

	public class BalancedTreeNode implements ITransparentTreeNode {
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
			BalancedTreeNode other = (BalancedTreeNode) obj;
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

		@Override
		public int getDepth() {
			return this.depth;
		}

		@Override
		public long getNumberOfLeftRelativesInSameGeneration() {
			return this.idOfNodeOnLayer;
		}

		@Override
		public long getNumberOfRightRelativesInSameGeneration() {
			return (long)(Math.pow(BalancedGraphGeneratorGenerator.this.branchingFactor, this.depth) - this.idOfNodeOnLayer - 1);
		}

		@Override
		public long getNumberOfLeafsStemmingFromLeftRelativesInSameGeneration() {
			return this.getNumberOfLeafsUnderNode() * this.getNumberOfLeftRelativesInSameGeneration();
		}

		@Override
		public long getNumberOfLeafsUnderNode() {
			return (long)Math.pow(BalancedGraphGeneratorGenerator.this.branchingFactor, BalancedGraphGeneratorGenerator.this.maxDepth - this.depth);
		}

		@Override
		public long getNumberOfLeafsStemmingFromRightRelativesInSameGeneration() {
			return this.getNumberOfLeafsUnderNode() * this.getNumberOfRightRelativesInSameGeneration();
		}

		@Override
		public int getDistanceToShallowestLeafUnderNode() {
			return BalancedGraphGeneratorGenerator.this.maxDepth - this.depth;
		}

		@Override
		public int getDistanceToDeepestLeafUnderNode() {
			return this.getDistanceToShallowestLeafUnderNode();
		}
	}

	public BalancedGraphGeneratorGenerator(final int branchingFactor, final int depth) {
		super();
		this.branchingFactor = branchingFactor;
		this.maxDepth = depth;
	}

	public IGraphGenerator<ITransparentTreeNode, Integer> create() {
		return new IGraphGenerator<ITransparentTreeNode, Integer>() {

			@Override
			public SingleRootGenerator<ITransparentTreeNode> getRootGenerator() {
				return BalancedTreeNode::new;
			}

			@Override
			public SingleSuccessorGenerator<ITransparentTreeNode, Integer> getSuccessorGenerator() {
				return new SingleSuccessorGenerator<ITransparentTreeNode, Integer>() {

					private Map<ITransparentTreeNode, Set<Integer>> successors = new HashMap<>();

					@Override
					public List<NodeExpansionDescription<ITransparentTreeNode, Integer>> generateSuccessors(final ITransparentTreeNode node) throws InterruptedException {
						List<NodeExpansionDescription<ITransparentTreeNode, Integer>> successorsOfThisNode = new ArrayList<>();
						int d = node.getDepth() + 1;
						if (d > BalancedGraphGeneratorGenerator.this.maxDepth) {
							return successorsOfThisNode;
						}
						for (int i = 0; i < BalancedGraphGeneratorGenerator.this.branchingFactor; i++) {
							successorsOfThisNode.add(this.generateSuccessor(node, i));
						}
						return successorsOfThisNode;
					}

					@Override
					public NodeExpansionDescription<ITransparentTreeNode, Integer> generateSuccessor(final ITransparentTreeNode node, final int i) throws InterruptedException {
						int j = i % BalancedGraphGeneratorGenerator.this.branchingFactor;
						int d = node.getDepth() + 1;
						long offsetForIdOnLayer = BalancedGraphGeneratorGenerator.this.branchingFactor * node.getNumberOfLeftRelativesInSameGeneration();
						BalancedTreeNode successor = new BalancedTreeNode();
						successor.depth = d;
						successor.idOfNodeOnLayer = offsetForIdOnLayer + j;
						this.successors.computeIfAbsent(node, n -> new HashSet<>()).add(j);
						return new NodeExpansionDescription<>(successor, j, NodeType.OR);
					}

					@Override
					public boolean allSuccessorsComputed(final ITransparentTreeNode node) {
						return this.successors.get(node) != null && this.successors.get(node).size() == BalancedGraphGeneratorGenerator.this.branchingFactor;
					}
				};
			}
		};
	}
}
