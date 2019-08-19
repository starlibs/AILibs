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

	public int getNumberOfLeafsUnderANonTerminalNodeInDepth(final int depthOfRequestedNode, final int assumedDepthOfTree) {
		int remainingDepth = assumedDepthOfTree - depthOfRequestedNode;
		int innerNodes = 0;
		for (int k = 0; k < remainingDepth; k++) {
			innerNodes += (int) Math.pow(DegeneratedGraphGeneratorGenerator.this.branchingFactor - DegeneratedGraphGeneratorGenerator.this.deadEndsPerGeneration, k);
		}
		int deadEndSolutions = innerNodes * DegeneratedGraphGeneratorGenerator.this.deadEndsPerGeneration;
		int leafs = (int) (Math.pow(DegeneratedGraphGeneratorGenerator.this.branchingFactor - DegeneratedGraphGeneratorGenerator.this.deadEndsPerGeneration, remainingDepth) + deadEndSolutions);
		//		System.out.println("Remaining depth: " + remainingDepth + ". Num inner nodes: " + innerNodes + ". Dead end solutions: " + deadEndSolutions + ". This yields " + leafs + " leaf nodes.");
		return leafs;
	}

	public int getNumberOfMaxSubtreesOfMaxLengthUnderNonTerminalNodeInDepth(final int depth, final long maxNumberOfNodes) {

		/* check from bottom to top how many nodes can be contained in a single limited sub tree */
		int height = 1;
		while (DegeneratedGraphGeneratorGenerator.this.getNumberOfLeafsUnderANonTerminalNodeInDepth(this.maxDepth - height, DegeneratedGraphGeneratorGenerator.this.maxDepth) < maxNumberOfNodes) {
			height ++;
		}
		height --;

		/* at this point, the height variable contains the height of a sub-tree that can serve as a island. Every node up to a level of maxDepth - height then serves as the root of a sub-graph */
		int depthOfLayer = this.maxDepth - height;

		/* now compute the number of leafs pretending that the roots of the sub graphs are leafs */
		//		System.out.println("Getting number of leafs of a sub-tree in depth " + depth + " up to a depth of " + depthOfLayer);
		return DegeneratedGraphGeneratorGenerator.this.getNumberOfLeafsUnderANonTerminalNodeInDepth(depth, depthOfLayer);
	}

	public class TreeNode implements ITransparentTreeNode {
		TreeNode parent;
		int depth;
		Set<Integer> indicesOfChildrenWithoutChildren;
		int idOfNodeAmongChildren;
		long idOfNodeOnLayer;
		long numOfLeftSiblingsThatHaveChildren;
		long numOfLeftRelativesThatHaveChildren;
		long numberOfLeafsFoundByDFSWhenReachingThisNode;
		boolean hasChildren;

		public TreeNode(final TreeNode parent, final int depth, final long idOfNodeOnLayer, final int idOfNodeAmongChildren, final long numOfLeftRelativesThatHaveChildren, final boolean hasChildren, final int numOfLeftSiblingsThatHaveChildren, final long solutionsPriorToThisNodeViaDFS) {
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
			result = prime * result + (int) this.idOfNodeOnLayer;
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
		public long getNumberOfLeafsPriorToNodeViaDFS() {
			return this.numberOfLeafsFoundByDFSWhenReachingThisNode;
		}

		@Override
		public long getNumberOfRightRelativesInSameGeneration() {
			return (long) (Math.pow(DegeneratedGraphGeneratorGenerator.this.branchingFactor, this.depth) - this.idOfNodeOnLayer - 1);
		}

		@Override
		public long getNumberOfLeafsStemmingFromLeftRelativesInSameGeneration() {
			throw new UnsupportedOperationException();
		}

		@Override
		public long getNumberOfLeafsStemmingFromRightRelativesInSameGeneration() {
			throw new UnsupportedOperationException();
		}

		@Override
		public long getNumberOfLeafsUnderNode() {
			if (!this.hasChildren) {
				return 1;
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
		public long getNumberOfSubtreesWithMaxNumberOfNodesPriorToThisNode(final long maxNumberOfNodes) {
			if (this.parent == null) {
				return 0;
			}

			/* get number of complete subtrees when arriving at the parent */
			long numSubtreesInducedByParentLevels = this.parent.getNumberOfSubtreesWithMaxNumberOfNodesPriorToThisNode(maxNumberOfNodes);

			/* if the parent consists of only one such sub-tree itself, return the just computed value */
			if (this.parent.getNumberOfLeafsUnderNode() <= maxNumberOfNodes) {
				return numSubtreesInducedByParentLevels;
			}

			/* otherwise, sum over the sub-trees of left siblings */
			long subTreesUnderLeftSiblings = 0;
			long maxNumberOfSubTreesForNonTerminalsOfThisDepth = DegeneratedGraphGeneratorGenerator.this.getNumberOfMaxSubtreesOfMaxLengthUnderNonTerminalNodeInDepth(this.depth, maxNumberOfNodes);
			//			System.out.println("max number of subtrees for non-terminals in depth " + this.depth + " is " + maxNumberOfSubTreesForNonTerminalsOfThisDepth);
			subTreesUnderLeftSiblings += this.numOfLeftSiblingsThatHaveChildren * maxNumberOfSubTreesForNonTerminalsOfThisDepth;
			subTreesUnderLeftSiblings += (this.idOfNodeAmongChildren - this.numOfLeftSiblingsThatHaveChildren);
			//			System.out.println(numSubtreesInducedByParentLevels + " + " + this.numOfLeftSiblingsThatHaveChildren + " * " + maxNumberOfSubTreesForNonTerminalsOfThisDepth + " + " + (this.idOfNodeAmongChildren - this.numOfLeftSiblingsThatHaveChildren) + "; depth = " + this.depth);
			return numSubtreesInducedByParentLevels + subTreesUnderLeftSiblings;
		}

		@Override
		public long getNumberOfSubtreesWithMaxNumberOfNodes(final long maxNumberOfNodes) {
			return DegeneratedGraphGeneratorGenerator.this.getNumberOfMaxSubtreesOfMaxLengthUnderNonTerminalNodeInDepth(this.depth, maxNumberOfNodes);
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
				return () -> new TreeNode(null, 0, 0, 0, 0, true, 0, 0);
			}

			@Override
			public SingleSuccessorGenerator<ITransparentTreeNode, Integer> getSuccessorGenerator() {
				return new SingleSuccessorGenerator<ITransparentTreeNode, Integer>() {

					private Map<ITransparentTreeNode, Set<Integer>> successors = new HashMap<>();

					@Override
					public List<NodeExpansionDescription<ITransparentTreeNode, Integer>> generateSuccessors(final ITransparentTreeNode node) throws InterruptedException {
						TreeNode tNode = (TreeNode) node;
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
						TreeNode tNode = (TreeNode) node;
						if (!tNode.hasChildren) {
							throw new IllegalArgumentException("Node " + node + " has no children and, hence, cannot have any successor being generated.");
						}
						int j = i % DegeneratedGraphGeneratorGenerator.this.branchingFactor; // note that j is also the number of left siblings
						int d = node.getDepth() + 1;

						/* compute offset of ids for successors under this node, and also the number of nodes left of the successor that have children */
						long offsetForIdOnLayer = DegeneratedGraphGeneratorGenerator.this.branchingFactor * tNode.numOfLeftRelativesThatHaveChildren;
						long numOfLeftRelativesThatHaveChildren = (DegeneratedGraphGeneratorGenerator.this.branchingFactor - DegeneratedGraphGeneratorGenerator.this.deadEndsPerGeneration) * tNode.numOfLeftRelativesThatHaveChildren;
						int numOfLeftSiblingsThatHaveChildren = 0;
						long numOfLeftSiblingsWithoutChildren = 0;
						for (int k = 0; k < j; k++) {
							if (!tNode.indicesOfChildrenWithoutChildren.contains(k)) {
								numOfLeftSiblingsThatHaveChildren++;
							} else {
								numOfLeftSiblingsWithoutChildren++; // these are leafs themselves
							}
						}
						numOfLeftRelativesThatHaveChildren += numOfLeftSiblingsThatHaveChildren;

						/* compute number of solutions found by DFS when reaching this node */
						long numOfSolutionsOfEveryLeftSiblingWithChildren = DegeneratedGraphGeneratorGenerator.this.getNumberOfLeafsUnderANonTerminalNodeInDepth(d, DegeneratedGraphGeneratorGenerator.this.maxDepth);
						long numOfSolutionsUnderLeftSiblings = numOfLeftSiblingsWithoutChildren + numOfLeftSiblingsThatHaveChildren * numOfSolutionsOfEveryLeftSiblingWithChildren;
						long numberOfSolutionsFoundByDFS = tNode.numberOfLeafsFoundByDFSWhenReachingThisNode + numOfSolutionsUnderLeftSiblings;

						/* check whether the node has children itself */
						boolean hasChildren = !tNode.indicesOfChildrenWithoutChildren.contains(i) && d < DegeneratedGraphGeneratorGenerator.this.maxDepth;

						/* create node */
						TreeNode successor = new TreeNode(tNode, d, offsetForIdOnLayer + j, j, numOfLeftRelativesThatHaveChildren, hasChildren, numOfLeftSiblingsThatHaveChildren, numberOfSolutionsFoundByDFS);
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
