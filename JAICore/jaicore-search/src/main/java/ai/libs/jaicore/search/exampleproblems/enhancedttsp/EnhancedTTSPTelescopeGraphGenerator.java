package ai.libs.jaicore.search.exampleproblems.enhancedttsp;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPBinaryTelescopeNode;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPBinaryTelescopeNode.EnhancedTTSPBinaryTelescopeDestinationDecisionNode;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPBinaryTelescopeNode.EnhancedTTSPBinaryTelescopeDeterminedDestinationNode;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPState;
import ai.libs.jaicore.search.model.NodeExpansionDescription;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class EnhancedTTSPTelescopeGraphGenerator implements IGraphGenerator<EnhancedTTSPBinaryTelescopeNode, String>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(EnhancedTTSPTelescopeGraphGenerator.class);

	private final EnhancedTTSP problem;

	public EnhancedTTSPTelescopeGraphGenerator(final EnhancedTTSP problem) {
		super();
		this.problem = problem;
		this.logMode();
	}

	private void logMode() {
		this.logger.info("Initialized {} with {} locations: {}", this.getClass().getSimpleName(), this.problem.getPossibleDestinations().size(), this.problem.getPossibleDestinations());
	}

	@Override
	public ISingleRootGenerator<EnhancedTTSPBinaryTelescopeNode> getRootGenerator() {
		return () -> new EnhancedTTSPBinaryTelescopeDeterminedDestinationNode(null, this.problem.getInitalState());
	}

	@Override
	public ISuccessorGenerator<EnhancedTTSPBinaryTelescopeNode, String> getSuccessorGenerator() {
		return new ISuccessorGenerator<EnhancedTTSPBinaryTelescopeNode, String>() {

			@Override
			public List<INewNodeDescription<EnhancedTTSPBinaryTelescopeNode, String>> generateSuccessors(final EnhancedTTSPBinaryTelescopeNode node) throws InterruptedException {
				long start = System.currentTimeMillis();
				EnhancedTTSPTelescopeGraphGenerator.this.logger.info("Computing successors of node {}", node);
				List<INewNodeDescription<EnhancedTTSPBinaryTelescopeNode, String>> l = new ArrayList<>();
				if (node.getCurTour().size() >= EnhancedTTSPTelescopeGraphGenerator.this.problem.getPossibleDestinations().size()) {
					EnhancedTTSPTelescopeGraphGenerator.this.logger.info("Cannot generate successors of a node in which we are in pos {} and in which have already visited everything!", node.getCurLocation());
					return l;
				}

				ShortList remainingTargets = EnhancedTTSPTelescopeGraphGenerator.this.problem.getPossibleRemainingDestinationsInState(node.getState());
				EnhancedTTSPTelescopeGraphGenerator.this.logger.debug("Remaining targets: {}", remainingTargets);
				short numberOfRemainingTargets = (short)(remainingTargets.size());
				short minDepth = (short)Math.floor(FastMath.log(2, numberOfRemainingTargets));
				int numberOfNodesOnMinDepthPlus1 = (numberOfRemainingTargets - (int)Math.pow(2, minDepth)) * 2;

				/* get bit sets for child nodes */
				List<Boolean> bitSetForLeftChild;
				List<Boolean> bitSetForRightChild;
				if (node instanceof EnhancedTTSPBinaryTelescopeDestinationDecisionNode) {
					EnhancedTTSPBinaryTelescopeDestinationDecisionNode cNode = (EnhancedTTSPBinaryTelescopeDestinationDecisionNode)node;
					bitSetForLeftChild = new ArrayList<>(cNode.getField());
					bitSetForRightChild = new ArrayList<>(cNode.getField());
					EnhancedTTSPTelescopeGraphGenerator.this.logger.debug("Cloning current BitVector {} into left and right.", bitSetForLeftChild);
				}
				else {
					EnhancedTTSPTelescopeGraphGenerator.this.logger.debug("Creating new BitVector in this state node.");
					bitSetForLeftChild = new ArrayList<>();
					bitSetForRightChild = new ArrayList<>();
				}
				bitSetForLeftChild.add(false);
				bitSetForRightChild.add(true);

				/* determine whether the children are leaf nodes */
				boolean leftChildIsLeaf = false;
				boolean rightChildIsLeaf = false;
				if (bitSetForLeftChild.size() >= minDepth) {
					if (numberOfRemainingTargets == 2 || bitSetForLeftChild.size() == minDepth + 1) {
						leftChildIsLeaf = true;
						rightChildIsLeaf = true;
					}
					else { // here, the left child has depth minDepth
						List<Boolean> leftChildOfLeftChild = new ArrayList<>(bitSetForLeftChild);
						leftChildOfLeftChild.add(false);
						long indexOfThatChild = convert(leftChildOfLeftChild);
						leftChildIsLeaf = indexOfThatChild >= numberOfNodesOnMinDepthPlus1;
						rightChildIsLeaf = leftChildIsLeaf || (indexOfThatChild + 2) >= numberOfNodesOnMinDepthPlus1;
					}
				}
				if (Thread.interrupted()) {
					throw new InterruptedException("Successor generation has been interrupted.");
				}
				EnhancedTTSPTelescopeGraphGenerator.this.logger.debug("Children bit-vectors are {}/{}. Leaf predicates are {}/{}", bitSetForLeftChild, bitSetForRightChild, leftChildIsLeaf, rightChildIsLeaf);

				/* compute left child */
				EnhancedTTSPBinaryTelescopeNode leftChild;
				if (leftChildIsLeaf) {
					short firstNextDestination = EnhancedTTSPTelescopeGraphGenerator.this.getDestinationBasedOnBitVectorAndAvailableDestinations(remainingTargets, bitSetForLeftChild);
					EnhancedTTSPTelescopeGraphGenerator.this.logger.debug("Determined next location {} (index {}) from bitvector {} for left child.", firstNextDestination, convert(bitSetForLeftChild), bitSetForLeftChild);
					EnhancedTTSPState successorStateForLeftChild = EnhancedTTSPTelescopeGraphGenerator.this.problem.computeSuccessorState(node.getState(), firstNextDestination);
					if (numberOfRemainingTargets == 2) { // if after this decision nothing can be decided anymore, enforce another state change
						short other = remainingTargets.stream().filter(s -> s != firstNextDestination).findAny().get();
						successorStateForLeftChild = EnhancedTTSPTelescopeGraphGenerator.this.problem.computeSuccessorState(successorStateForLeftChild, other);
						successorStateForLeftChild = EnhancedTTSPTelescopeGraphGenerator.this.problem.computeSuccessorState(successorStateForLeftChild, EnhancedTTSPTelescopeGraphGenerator.this.problem.getStartLocation());
					}
					leftChild = new EnhancedTTSPBinaryTelescopeDeterminedDestinationNode(node, successorStateForLeftChild);
				}
				else {
					leftChild = new EnhancedTTSPBinaryTelescopeDestinationDecisionNode(node, false);
				}

				/* compute right child */
				EnhancedTTSPBinaryTelescopeNode rightChild;
				if (rightChildIsLeaf) {
					short firstNextDestination = EnhancedTTSPTelescopeGraphGenerator.this.getDestinationBasedOnBitVectorAndAvailableDestinations(remainingTargets, bitSetForRightChild);
					EnhancedTTSPTelescopeGraphGenerator.this.logger.debug("Determined next location {} (index {}) from bitvector {} for right child.", firstNextDestination, convert(bitSetForRightChild), bitSetForRightChild);
					EnhancedTTSPState successorStateForRightChild = EnhancedTTSPTelescopeGraphGenerator.this.problem.computeSuccessorState(node.getState(), firstNextDestination);
					if (numberOfRemainingTargets == 2) {
						short other = remainingTargets.stream().filter(s -> s != firstNextDestination).findAny().get();
						successorStateForRightChild = EnhancedTTSPTelescopeGraphGenerator.this.problem.computeSuccessorState(successorStateForRightChild, other);
						successorStateForRightChild = EnhancedTTSPTelescopeGraphGenerator.this.problem.computeSuccessorState(successorStateForRightChild, EnhancedTTSPTelescopeGraphGenerator.this.problem.getStartLocation());
					}
					rightChild = new EnhancedTTSPBinaryTelescopeDeterminedDestinationNode(node, successorStateForRightChild);
				}
				else {
					rightChild = new EnhancedTTSPBinaryTelescopeDestinationDecisionNode(node, true);
				}
				l.add(new NodeExpansionDescription<>(leftChild, "l"));
				l.add(new NodeExpansionDescription<>(rightChild, "r"));
				long walltime = System.currentTimeMillis() - start;
				if (walltime > 10) {
					EnhancedTTSPTelescopeGraphGenerator.this.logger.warn("Successor generation took {}ms", walltime);
				}
				return l;
			}
		};
	}

	public short getDestinationBasedOnBitVectorAndAvailableDestinations(final ShortList destinations, final List<Boolean> vector) {
		short pathIndex = (short)convert(vector);
		short numberOfRemainingTargets = (short)(destinations.size());
		short minDepth = (short)Math.floor(FastMath.log(2, numberOfRemainingTargets));
		int numberOfNodesAddedOnLastLayer = (numberOfRemainingTargets - (int)Math.pow(2, minDepth));
		short correctedPathIndex = vector.size() == (minDepth + 1) ? pathIndex : (short)(pathIndex + numberOfNodesAddedOnLastLayer);
		return destinations.getShort(correctedPathIndex);
	}

	public static long convert(final List<Boolean> bits) {
		long value = 0L;
		for (int i = 0; i < bits.size(); ++i) {
			value += bits.get(i).booleanValue() ? (1L << (bits.size() - i - 1)) : 0L;
		}
		return value;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		this.logMode();
	}
}
