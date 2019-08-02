package ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.NodeExpansionDescription;
import org.api4.java.datastructure.graph.implicit.NodeType;
import org.api4.java.datastructure.graph.implicit.SingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.SingleSuccessorGenerator;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;

class DegeneratedGraphGeneratorGenerator {

	private final Random random;
	private final int deadEndsPerGeneration;
	private final int branchingFactor;
	private final int maxDepth;

	public class TreeNode implements ITransparentTreeNode {
		int depth;
		Set<Integer> indicesOfChildrenWithoutChildren;
		long idOfNodeOnLayer;
		boolean hasChildren;

		public TreeNode(final int depth, final long idOfNodeOnLayer, final boolean hasChildren) {
			super();
			this.depth = depth;
			this.idOfNodeOnLayer = idOfNodeOnLayer;
			this.hasChildren = hasChildren;
			this.indicesOfChildrenWithoutChildren = new HashSet<>();
			if (hasChildren) {
				while (this.indicesOfChildrenWithoutChildren.size() < DegeneratedGraphGeneratorGenerator.this.deadEndsPerGeneration) {
					this.indicesOfChildrenWithoutChildren.add(DegeneratedGraphGeneratorGenerator.this.random.nextInt(DegeneratedGraphGeneratorGenerator.this.branchingFactor));
				}
			}
		}

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
			TreeNode other = (TreeNode) obj;
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
			return (long)(Math.pow(DegeneratedGraphGeneratorGenerator.this.branchingFactor, this.depth) - this.idOfNodeOnLayer - 1);
		}

		@Override
		public long getNumberOfLeafsStemmingFromLeftRelativesInSameGeneration() {
			return this.getNumberOfLeafsUnderNode() * this.getNumberOfLeftRelativesInSameGeneration();
		}

		@Override
		public long getNumberOfLeafsUnderNode() {
			return (long)Math.pow(DegeneratedGraphGeneratorGenerator.this.branchingFactor, DegeneratedGraphGeneratorGenerator.this.maxDepth - this.depth);
		}

		@Override
		public long getNumberOfLeafsStemmingFromRightRelativesInSameGeneration() {
			return this.getNumberOfLeafsUnderNode() * this.getNumberOfRightRelativesInSameGeneration();
		}

		@Override
		public int getDistanceToShallowestLeafUnderNode() {
			return DegeneratedGraphGeneratorGenerator.this.maxDepth - this.depth;
		}

		@Override
		public int getDistanceToDeepestLeafUnderNode() {
			return this.getDistanceToShallowestLeafUnderNode();
		}
	}

	public DegeneratedGraphGeneratorGenerator(final Random random, final int deadEndsPerGeneration, final int branchingFactor, final int maxDepth) {
		super();
		this.random = random;
		this.deadEndsPerGeneration = deadEndsPerGeneration;
		this.branchingFactor = branchingFactor;
		this.maxDepth = maxDepth;
	}

	public IGraphGenerator<ITransparentTreeNode, Integer> create() {
		return new IGraphGenerator<ITransparentTreeNode, Integer>() {

			@Override
			public SingleRootGenerator<ITransparentTreeNode> getRootGenerator() {
				return () -> new TreeNode(0, 0, true);
			}

			@Override
			public SingleSuccessorGenerator<ITransparentTreeNode, Integer> getSuccessorGenerator() {
				return new SingleSuccessorGenerator<ITransparentTreeNode, Integer>() {

					private Map<ITransparentTreeNode, Set<Integer>> successors = new HashMap<>();

					@Override
					public List<NodeExpansionDescription<ITransparentTreeNode, Integer>> generateSuccessors(final ITransparentTreeNode node) throws InterruptedException {
						TreeNode tNode = (TreeNode)node;
						List<NodeExpansionDescription<ITransparentTreeNode, Integer>> successorsOfThisNode = new ArrayList<>();
						if (!tNode.hasChildren) {
							return successorsOfThisNode;
						}
						int d = node.getDepth() + 1;
						if (d > DegeneratedGraphGeneratorGenerator.this.maxDepth) {
							return successorsOfThisNode;
						}
						for (int i = 0; i < DegeneratedGraphGeneratorGenerator.this.branchingFactor; i++) {
							successorsOfThisNode.add(this.generateSuccessor(node, i));
						}
						return successorsOfThisNode;
					}

					@Override
					public NodeExpansionDescription<ITransparentTreeNode, Integer> generateSuccessor(final ITransparentTreeNode node, final int i) throws InterruptedException {
						TreeNode tNode = (TreeNode)node;
						if (!tNode.hasChildren) {
							throw new IllegalArgumentException("Node " + node + " has no children and, hence, cannot have any successor being generated.");
						}
						int j = i % DegeneratedGraphGeneratorGenerator.this.branchingFactor;
						int d = node.getDepth() + 1;
						long offsetForIdOnLayer = DegeneratedGraphGeneratorGenerator.this.branchingFactor * node.getNumberOfLeftRelativesInSameGeneration();
						boolean hasChildren = !tNode.indicesOfChildrenWithoutChildren.contains(i);
						TreeNode successor = new TreeNode(d, offsetForIdOnLayer + j, hasChildren);
						this.successors.computeIfAbsent(node, n -> new HashSet<>()).add(j);
						return new NodeExpansionDescription<>(successor, j, NodeType.OR);
					}

					@Override
					public boolean allSuccessorsComputed(final ITransparentTreeNode node) {
						return this.successors.get(node) != null && this.successors.get(node).size() == DegeneratedGraphGeneratorGenerator.this.branchingFactor;
					}
				};
			}
		};
	}
}
