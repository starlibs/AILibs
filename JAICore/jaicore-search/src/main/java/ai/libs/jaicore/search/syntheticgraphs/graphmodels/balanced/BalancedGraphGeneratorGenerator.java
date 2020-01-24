package ai.libs.jaicore.search.syntheticgraphs.graphmodels.balanced;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.ILazySuccessorGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;

import ai.libs.jaicore.basic.MappingIterator;
import ai.libs.jaicore.search.model.NodeExpansionDescription;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;

public class BalancedGraphGeneratorGenerator {

	private final int branchingFactor;
	private final int maxDepth;

	public static int getNumberOfLeafsUnderANonTerminalNodeInDepth(final int depthOfRequestedNode, final int branchingFactor, final int assumedDepthOfTree) {
		return (int)Math.pow(branchingFactor, assumedDepthOfTree - (double)depthOfRequestedNode);
	}

	public static BigInteger getNumberOfMaxSubtreesOfMaxLengthUnderNonTerminalNodeInDepth(final int depth, final BigInteger maxNumberOfNodes, final int branchingFactor, final int maxDepth) {
		if (depth >= maxDepth) {
			throw new IllegalArgumentException("A node in depth " + depth + " in a graph with max depth " + maxDepth + " cannot be an inner nodee!");
		}

		/* determine possible height */
		int height = 0;
		BigInteger numberOfNodesForHeight = BigInteger.ONE;
		while (numberOfNodesForHeight.compareTo(maxNumberOfNodes) <= 0 && height < maxDepth) {
			height ++;
			numberOfNodesForHeight = BigInteger.valueOf(branchingFactor).pow(height);
		}
		height --;
		int missingLayers = maxDepth - depth;
		return BigInteger.valueOf(branchingFactor).pow(missingLayers - height);
	}

	public BigInteger getNumberOfMaxSubtreesOfMaxLengthUnderNonTerminalNodeInDepth(final int depth, final BigInteger maxNumberOfNodes) {
		return getNumberOfMaxSubtreesOfMaxLengthUnderNonTerminalNodeInDepth(depth, maxNumberOfNodes, this.branchingFactor, this.maxDepth);
	}


	public class BalancedTreeNode implements ITransparentTreeNode {
		protected final int depth;
		protected final BigInteger idOfNodeOnLayer;

		public BalancedTreeNode(final int depth, final BigInteger idOfNodeOnLayer) {
			super();
			this.depth = depth;
			this.idOfNodeOnLayer = idOfNodeOnLayer;
		}

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
			return this.getNumberOfLeafsPriorToNodeViaDFS().divideAndRemainder(maxNumberOfNodes)[0]; // here we can exploit the special structure of the balanced tree
		}

		@Override
		public BigInteger getNumberOfSubtreesWithMaxNumberOfNodes(final BigInteger maxNumberOfNodes) {
			return BalancedGraphGeneratorGenerator.this.getNumberOfMaxSubtreesOfMaxLengthUnderNonTerminalNodeInDepth(this.depth, maxNumberOfNodes);
		}

		@Override
		public BigInteger getNumberOfLeafsPriorToNodeViaDFS() {
			if (this.depth == BalancedGraphGeneratorGenerator.this.maxDepth) {
				return this.getNumberOfLeftRelativesInSameGeneration();
			}
			else {
				return this.getNumberOfLeftRelativesInSameGeneration().multiply(BigInteger.valueOf(BalancedGraphGeneratorGenerator.getNumberOfLeafsUnderANonTerminalNodeInDepth(this.depth, BalancedGraphGeneratorGenerator.this.branchingFactor, BalancedGraphGeneratorGenerator.this.maxDepth)));
			}
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
			public ISingleRootGenerator<ITransparentTreeNode> getRootGenerator() {
				return () -> new BalancedTreeNode(0, BigInteger.ZERO);
			}

			@Override
			public ILazySuccessorGenerator<ITransparentTreeNode, Integer> getSuccessorGenerator() {
				return new ILazySuccessorGenerator<ITransparentTreeNode, Integer>() {

					private Map<ITransparentTreeNode, Set<Integer>> successors = new HashMap<>();

					@Override
					public List<INewNodeDescription<ITransparentTreeNode, Integer>> generateSuccessors(final ITransparentTreeNode node) {
						List<INewNodeDescription<ITransparentTreeNode, Integer>> successorsOfThisNode = new ArrayList<>();
						int d = node.getDepth() + 1;
						if (d > BalancedGraphGeneratorGenerator.this.maxDepth) {
							return successorsOfThisNode;
						}
						for (int i = 0; i < BalancedGraphGeneratorGenerator.this.branchingFactor; i++) {
							successorsOfThisNode.add(this.generateSuccessor(node, i));
						}
						return successorsOfThisNode;
					}

					public NodeExpansionDescription<ITransparentTreeNode, Integer> generateSuccessor(final ITransparentTreeNode node, final int i) {
						int j = i % BalancedGraphGeneratorGenerator.this.branchingFactor;
						int d = node.getDepth() + 1;
						BigInteger leftRelativesInGenerationOfNode = node.getNumberOfLeftRelativesInSameGeneration();
						Objects.requireNonNull(leftRelativesInGenerationOfNode);
						BigInteger offsetForIdOnLayer = BigInteger.valueOf(BalancedGraphGeneratorGenerator.this.branchingFactor).multiply(leftRelativesInGenerationOfNode);
						BalancedTreeNode successor = new BalancedTreeNode(d, offsetForIdOnLayer.add(BigInteger.valueOf(j)));
						this.successors.computeIfAbsent(node, n -> new HashSet<>()).add(j);
						return new NodeExpansionDescription<>(successor, j);
					}

					@Override
					public Iterator<INewNodeDescription<ITransparentTreeNode, Integer>> getIterativeGenerator(final ITransparentTreeNode node) {
						return new MappingIterator<>(IntStream.range(0, BalancedGraphGeneratorGenerator.this.branchingFactor).iterator(), i -> this.generateSuccessor(node, i));
					}
				};
			}
		};
	}
}
