package ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.ILazySuccessorGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;

import ai.libs.jaicore.basic.MappingIterator;
import ai.libs.jaicore.search.model.NodeExpansionDescription;
import ai.libs.jaicore.search.syntheticgraphs.ISyntheticGraphGeneratorBuilder;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;

public class DegeneratedGraphGeneratorGenerator implements ISyntheticGraphGeneratorBuilder {

	private final Random random;
	private final int deadEndsPerGeneration;
	private final int branchingFactor;
	private final int maxDepth;

	public BigInteger getNumberOfLeafsUnderANonTerminalNodeInDepth(final int depthOfRequestedNode, final int assumedDepthOfTree) {
		if (depthOfRequestedNode > assumedDepthOfTree) {
			throw new IllegalArgumentException("Requested node must not be deeper than the assumed depth of the tree!");
		}
		int remainingDepth = assumedDepthOfTree - depthOfRequestedNode;
		BigInteger innerNodes = BigInteger.ZERO;
		for (int k = 0; k < remainingDepth; k++) {
			innerNodes = innerNodes.add(BigInteger.valueOf(DegeneratedGraphGeneratorGenerator.this.branchingFactor - DegeneratedGraphGeneratorGenerator.this.deadEndsPerGeneration).pow(k));
		}
		BigInteger deadEndSolutions = innerNodes.multiply(new BigInteger("" + DegeneratedGraphGeneratorGenerator.this.deadEndsPerGeneration));
		//		System.out.println("Remaining depth: " + remainingDepth + ". Num inner nodes: " + innerNodes + ". Dead end solutions: " + deadEndSolutions + ". This yields xxx leaf nodes.");
		BigInteger leafs = BigInteger.valueOf(DegeneratedGraphGeneratorGenerator.this.branchingFactor - DegeneratedGraphGeneratorGenerator.this.deadEndsPerGeneration).pow(remainingDepth).add(deadEndSolutions);
		return leafs;
	}

	public BigInteger getNumberOfMaxSubtreesOfMaxLengthUnderNonTerminalNodeInDepth(final int depth, final BigInteger maxNumberOfNodes) {

		/* check from bottom to top how many nodes can be contained in a single limited sub tree */
		int height = 1;
		while (DegeneratedGraphGeneratorGenerator.this.getNumberOfLeafsUnderANonTerminalNodeInDepth(this.maxDepth - height, DegeneratedGraphGeneratorGenerator.this.maxDepth).compareTo(maxNumberOfNodes) < 0) {
			height ++;
		}
		height --;
		if (height > this.maxDepth) {
			throw new IllegalStateException("The height of the subtree cannot be higher than the max depth of the tree.");
		}

		/* at this point, the height variable contains the height of a sub-tree that can serve as a island. Every node up to a level of maxDepth - height then serves as the root of a sub-graph */
		int depthOfLayer = this.maxDepth - height;
		if (depthOfLayer < depth) {
			return BigInteger.ZERO;
			//			throw new IllegalStateException("The depth of the layer in which the nodes representing sub-trees lie cannot be smaller than the given depth.");
		}

		/* now compute the number of leafs pretending that the roots of the sub graphs are leafs */
		//		System.out.println("Getting number of leafs of a sub-tree in depth " + depth + " up to a depth of " + depthOfLayer);
		return DegeneratedGraphGeneratorGenerator.this.getNumberOfLeafsUnderANonTerminalNodeInDepth(depth, depthOfLayer);
	}

	public BigInteger getNumberOfLeafsInEverySubtreeOfMaxLength(final BigInteger maxNumberOfNodes) {

		/* check from bottom to top how many nodes can be contained in a single limited sub tree */
		int height = 1;
		while (DegeneratedGraphGeneratorGenerator.this.getNumberOfLeafsUnderANonTerminalNodeInDepth(this.maxDepth - height, DegeneratedGraphGeneratorGenerator.this.maxDepth).compareTo(maxNumberOfNodes) < 0) {
			height ++;
		}
		height --;
		return this.getNumberOfLeafsUnderANonTerminalNodeInDepth(0, height);
	}

	public class TreeNode implements ITransparentTreeNode {
		TreeNode parent;
		int depth;
		Set<Integer> indicesOfChildrenWithoutChildren;
		int idOfNodeAmongChildren;
		BigInteger idOfNodeOnLayer;
		int numOfLeftSiblingsThatHaveChildren;
		BigInteger numOfLeftRelativesThatHaveChildren;
		BigInteger numberOfLeafsFoundByDFSWhenReachingThisNode;
		boolean hasChildren;

		public TreeNode(final TreeNode parent, final int depth, final BigInteger idOfNodeOnLayer, final int idOfNodeAmongChildren, final BigInteger numOfLeftRelativesThatHaveChildren, final boolean hasChildren, final int numOfLeftSiblingsThatHaveChildren, final BigInteger solutionsPriorToThisNodeViaDFS) {
			super();
			this.parent = parent;
			this.depth = depth;
			this.idOfNodeAmongChildren = idOfNodeAmongChildren;
			this.idOfNodeOnLayer = idOfNodeOnLayer;
			this.numOfLeftRelativesThatHaveChildren = numOfLeftRelativesThatHaveChildren;
			this.hasChildren = hasChildren;
			this.numOfLeftSiblingsThatHaveChildren = numOfLeftSiblingsThatHaveChildren;
			this.numberOfLeafsFoundByDFSWhenReachingThisNode = solutionsPriorToThisNodeViaDFS;
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
			result = prime * result + this.idOfNodeOnLayer.hashCode();
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
			if (!this.idOfNodeOnLayer.equals(other.idOfNodeOnLayer)) {
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
		public BigInteger getNumberOfLeafsPriorToNodeViaDFS() {
			return this.numberOfLeafsFoundByDFSWhenReachingThisNode;
		}

		@Override
		public BigInteger getNumberOfRightRelativesInSameGeneration() {
			return BigInteger.valueOf(DegeneratedGraphGeneratorGenerator.this.branchingFactor).pow(this.depth).subtract(this.idOfNodeOnLayer).subtract(BigInteger.valueOf(1));
		}

		@Override
		public BigInteger getNumberOfLeafsStemmingFromLeftRelativesInSameGeneration() {
			throw new UnsupportedOperationException();
		}

		@Override
		public BigInteger getNumberOfLeafsStemmingFromRightRelativesInSameGeneration() {
			throw new UnsupportedOperationException();
		}

		@Override
		public BigInteger getNumberOfLeafsUnderNode() {
			if (!this.hasChildren) {
				return BigInteger.valueOf(1);
			}
			return DegeneratedGraphGeneratorGenerator.this.getNumberOfLeafsUnderANonTerminalNodeInDepth(this.depth, DegeneratedGraphGeneratorGenerator.this.maxDepth);
		}

		@Override
		public int getDistanceToShallowestLeafUnderNode() {
			return 0;
		}

		@Override
		public int getDistanceToDeepestLeafUnderNode() {
			return DegeneratedGraphGeneratorGenerator.this.maxDepth - this.depth;
		}

		@Override
		public BigInteger getNumberOfSubtreesWithMaxNumberOfNodesPriorToThisNode(final BigInteger maxNumberOfNodes) {
			if (this.parent == null) {
				return BigInteger.valueOf(0);
			}

			/* get number of complete subtrees when arriving at the parent */
			BigInteger numSubtreesInducedByParentLevels = this.parent.getNumberOfSubtreesWithMaxNumberOfNodesPriorToThisNode(maxNumberOfNodes);

			/* if the parent consists of only one such sub-tree itself, return the just computed value */
			if (this.parent.getNumberOfLeafsUnderNode().compareTo(maxNumberOfNodes) <= 0) {
				return numSubtreesInducedByParentLevels;
			}

			/* otherwise, sum over the sub-trees of left siblings */
			BigInteger maxNumberOfSubTreesForNonTerminalsOfThisDepth = DegeneratedGraphGeneratorGenerator.this.getNumberOfMaxSubtreesOfMaxLengthUnderNonTerminalNodeInDepth(this.depth, maxNumberOfNodes);
			BigInteger subTreesUnderLeftSiblings = maxNumberOfSubTreesForNonTerminalsOfThisDepth.multiply(BigInteger.valueOf(this.numOfLeftSiblingsThatHaveChildren));
			subTreesUnderLeftSiblings = subTreesUnderLeftSiblings.add(BigInteger.valueOf(this.idOfNodeAmongChildren - this.numOfLeftSiblingsThatHaveChildren));
			return numSubtreesInducedByParentLevels.add(subTreesUnderLeftSiblings);
		}

		@Override
		public BigInteger getNumberOfLeafsInSubtreesWithMaxNumberOfNodesPriorToThisNode(final BigInteger maxNumberOfNodes) {
			return DegeneratedGraphGeneratorGenerator.this.getNumberOfLeafsInEverySubtreeOfMaxLength(maxNumberOfNodes).multiply(this.getNumberOfSubtreesWithMaxNumberOfNodesPriorToThisNode(maxNumberOfNodes));
		}

		@Override
		public BigInteger getNumberOfSubtreesWithMaxNumberOfNodes(final BigInteger maxNumberOfNodes) {
			return DegeneratedGraphGeneratorGenerator.this.getNumberOfMaxSubtreesOfMaxLengthUnderNonTerminalNodeInDepth(this.depth, maxNumberOfNodes);
		}

		@Override
		public boolean hasChildren() {
			return this.hasChildren;
		}
	}

	public DegeneratedGraphGeneratorGenerator(final Random random, final int deadEndsPerGeneration, final int branchingFactor, final int maxDepth) {
		super();
		this.random = random;
		this.deadEndsPerGeneration = deadEndsPerGeneration;
		this.branchingFactor = branchingFactor;
		this.maxDepth = maxDepth;
	}

	@Override
	public IGraphGenerator<ITransparentTreeNode, Integer> build() {
		return new IGraphGenerator<ITransparentTreeNode, Integer>() {

			@Override
			public ISingleRootGenerator<ITransparentTreeNode> getRootGenerator() {
				return () -> new TreeNode(null, 0, BigInteger.ZERO, 0, BigInteger.ZERO, true, 0, BigInteger.ZERO);
			}

			@Override
			public ILazySuccessorGenerator<ITransparentTreeNode, Integer> getSuccessorGenerator() {
				return new ILazySuccessorGenerator<ITransparentTreeNode, Integer>() {

					private Map<ITransparentTreeNode, Set<Integer>> successors = new HashMap<>();

					@Override
					public List<INewNodeDescription<ITransparentTreeNode, Integer>> generateSuccessors(final ITransparentTreeNode node) throws InterruptedException {
						TreeNode tNode = (TreeNode) node;
						List<INewNodeDescription<ITransparentTreeNode, Integer>> successorsOfThisNode = new ArrayList<>();
						if (!tNode.hasChildren) {
							return successorsOfThisNode;
						}
						int d = node.getDepth() + 1;
						if (d > DegeneratedGraphGeneratorGenerator.this.maxDepth) {
							return successorsOfThisNode;
						}
						Iterator<INewNodeDescription<ITransparentTreeNode, Integer>> it = this.getIterativeGenerator(node);
						while (it.hasNext()) {
							successorsOfThisNode.add(it.next());
						}
						return successorsOfThisNode;
					}

					private INewNodeDescription<ITransparentTreeNode, Integer> getSuccessor(final ITransparentTreeNode node, final int indexOfChild) {
						TreeNode tNode = (TreeNode) node;
						if (!tNode.hasChildren) {
							throw new IllegalArgumentException("Node " + node + " has no children and, hence, cannot have any successor being generated.");
						}
						int j = indexOfChild % DegeneratedGraphGeneratorGenerator.this.branchingFactor; // note that j is also the number of left siblings
						int d = node.getDepth() + 1;

						/* compute offset of ids for successors under this node, and also the number of nodes left of the successor that have children */
						BigInteger offsetForIdOnLayer =  tNode.numOfLeftRelativesThatHaveChildren.multiply(BigInteger.valueOf(DegeneratedGraphGeneratorGenerator.this.branchingFactor));
						BigInteger numOfLeftRelativesThatHaveChildren = tNode.numOfLeftRelativesThatHaveChildren.multiply(BigInteger.valueOf(DegeneratedGraphGeneratorGenerator.this.branchingFactor - DegeneratedGraphGeneratorGenerator.this.deadEndsPerGeneration));
						int numOfLeftSiblingsThatHaveChildren = 0;
						long numOfLeftSiblingsWithoutChildren = 0;
						for (int k = 0; k < j; k++) {
							if (!tNode.indicesOfChildrenWithoutChildren.contains(k)) {
								numOfLeftSiblingsThatHaveChildren++;
							} else {
								numOfLeftSiblingsWithoutChildren++; // these are leafs themselves
							}
						}
						BigInteger numOfLeftSiblingsWithChildrenAsBigInt = BigInteger.valueOf(numOfLeftSiblingsThatHaveChildren);
						numOfLeftRelativesThatHaveChildren = numOfLeftRelativesThatHaveChildren.add(numOfLeftSiblingsWithChildrenAsBigInt);

						/* compute number of solutions found by DFS when reaching this node */
						BigInteger numOfSolutionsOfEveryLeftSiblingWithChildren = DegeneratedGraphGeneratorGenerator.this.getNumberOfLeafsUnderANonTerminalNodeInDepth(d, DegeneratedGraphGeneratorGenerator.this.maxDepth);
						BigInteger numOfSolutionsUnderLeftSiblings = numOfSolutionsOfEveryLeftSiblingWithChildren.multiply(numOfLeftSiblingsWithChildrenAsBigInt).add(BigInteger.valueOf(numOfLeftSiblingsWithoutChildren));
						BigInteger numberOfSolutionsFoundByDFS = tNode.numberOfLeafsFoundByDFSWhenReachingThisNode.add(numOfSolutionsUnderLeftSiblings);

						/* check whether the node has children itself */
						boolean hasChildren = !tNode.indicesOfChildrenWithoutChildren.contains(indexOfChild) && d < DegeneratedGraphGeneratorGenerator.this.maxDepth;

						/* create node */
						TreeNode successor = new TreeNode(tNode, d, offsetForIdOnLayer.add(BigInteger.valueOf(j)), j, numOfLeftRelativesThatHaveChildren, hasChildren, numOfLeftSiblingsThatHaveChildren, numberOfSolutionsFoundByDFS);
						this.successors.computeIfAbsent(node, n -> new HashSet<>()).add(j);
						return new NodeExpansionDescription<>(successor, j);
					}

					@Override
					public Iterator<INewNodeDescription<ITransparentTreeNode, Integer>> getIterativeGenerator(final ITransparentTreeNode node) {
						return new MappingIterator<>(IntStream.range(0, DegeneratedGraphGeneratorGenerator.this.branchingFactor).iterator(), i -> this.getSuccessor(node, i));
					}
				};
			}
		};
	}
}
