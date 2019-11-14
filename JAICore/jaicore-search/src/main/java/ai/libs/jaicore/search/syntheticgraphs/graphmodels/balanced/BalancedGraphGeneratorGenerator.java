package ai.libs.jaicore.search.syntheticgraphs.graphmodels.balanced;

import java.math.BigInteger;
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

	public int getNumberOfLeafsUnderANonTerminalNodeInDepth(final int depthOfRequestedNode, final int assumedDepthOfTree) {
		return (int)Math.pow(this.branchingFactor, assumedDepthOfTree - depthOfRequestedNode);
	}

	public BigInteger getNumberOfMaxSubtreesOfMaxLengthUnderNonTerminalNodeInDepth(final int depth, final BigInteger maxNumberOfNodes) {

		/* determine possible height */
		int height = 0;
		BigInteger numberOfNodesForHeight = BigInteger.ONE;
		while (numberOfNodesForHeight.compareTo(maxNumberOfNodes) < 0) {
			height ++;
			numberOfNodesForHeight = BigInteger.valueOf(BalancedGraphGeneratorGenerator.this.branchingFactor).pow(height);
		}
		height --;
		int missingLayers = BalancedGraphGeneratorGenerator.this.maxDepth - depth;
		return BigInteger.valueOf(BalancedGraphGeneratorGenerator.this.branchingFactor).pow(missingLayers - height);
	}

	public class BalancedTreeNode implements ITransparentTreeNode {
		int depth;
		BigInteger idOfNodeOnLayer;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.getEnclosingInstance().hashCode();
			result = prime * result + this.depth;
			result = prime * result + ((this.idOfNodeOnLayer == null) ? 0 : this.idOfNodeOnLayer.hashCode());
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
			if (!this.getEnclosingInstance().equals(other.getEnclosingInstance())) {
				return false;
			}
			if (this.depth != other.depth) {
				return false;
			}
			if (this.idOfNodeOnLayer == null) {
				if (other.idOfNodeOnLayer != null) {
					return false;
				}
			} else if (!this.idOfNodeOnLayer.equals(other.idOfNodeOnLayer)) {
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
		public BigInteger getNumberOfLeftRelativesInSameGeneration() {
			return this.idOfNodeOnLayer;
		}

		@Override
		public BigInteger getNumberOfRightRelativesInSameGeneration() {
			return BigInteger.valueOf(BalancedGraphGeneratorGenerator.this.branchingFactor).pow(this.depth).subtract(this.idOfNodeOnLayer).subtract(BigInteger.ONE);
		}

		@Override
		public BigInteger getNumberOfLeafsStemmingFromLeftRelativesInSameGeneration() {
			return this.getNumberOfLeafsUnderNode().multiply(this.getNumberOfLeftRelativesInSameGeneration());
		}

		@Override
		public BigInteger getNumberOfLeafsUnderNode() {
			return BigInteger.valueOf(BalancedGraphGeneratorGenerator.this.branchingFactor).pow(BalancedGraphGeneratorGenerator.this.maxDepth - this.depth);
		}

		@Override
		public BigInteger getNumberOfLeafsStemmingFromRightRelativesInSameGeneration() {
			return this.getNumberOfLeafsUnderNode().multiply(this.getNumberOfRightRelativesInSameGeneration());
		}

		@Override
		public int getDistanceToShallowestLeafUnderNode() {
			return BalancedGraphGeneratorGenerator.this.maxDepth - this.depth;
		}

		@Override
		public int getDistanceToDeepestLeafUnderNode() {
			return this.getDistanceToShallowestLeafUnderNode();
		}

		@Override
		public BigInteger getNumberOfSubtreesWithMaxNumberOfNodesPriorToThisNode(final BigInteger maxNumberOfNodes) {
			throw new UnsupportedOperationException();
		}

		@Override
		public BigInteger getNumberOfSubtreesWithMaxNumberOfNodes(final BigInteger maxNumberOfNodes) {
			return BalancedGraphGeneratorGenerator.this.getNumberOfMaxSubtreesOfMaxLengthUnderNonTerminalNodeInDepth(this.depth, maxNumberOfNodes);
		}

		@Override
		public BigInteger getNumberOfLeafsPriorToNodeViaDFS() {
			throw new UnsupportedOperationException();
		}

		@Override
		public BigInteger getNumberOfLeafsInSubtreesWithMaxNumberOfNodesPriorToThisNode(final BigInteger maxNumberOfNodes) {
			throw new UnsupportedOperationException();
		}

		private BalancedGraphGeneratorGenerator getEnclosingInstance() {
			return BalancedGraphGeneratorGenerator.this;
		}

		@Override
		public boolean hasChildren() {
			throw new UnsupportedOperationException();
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
						BigInteger offsetForIdOnLayer = BigInteger.valueOf(BalancedGraphGeneratorGenerator.this.branchingFactor).multiply(node.getNumberOfLeftRelativesInSameGeneration());
						BalancedTreeNode successor = new BalancedTreeNode();
						successor.depth = d;
						successor.idOfNodeOnLayer = offsetForIdOnLayer.add(BigInteger.valueOf(j));
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
