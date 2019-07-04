package ai.libs.jaicore.search.testproblems.enhancedttsp;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.ILoggingCustomizable;
import ai.libs.jaicore.search.core.interfaces.GraphGenerator;
import ai.libs.jaicore.search.model.travesaltree.NodeExpansionDescription;
import ai.libs.jaicore.search.model.travesaltree.NodeType;
import ai.libs.jaicore.search.structure.graphgenerator.NodeGoalTester;
import ai.libs.jaicore.search.structure.graphgenerator.SingleRootGenerator;
import ai.libs.jaicore.search.structure.graphgenerator.SingleSuccessorGenerator;
import ai.libs.jaicore.search.structure.graphgenerator.SuccessorGenerator;
import ai.libs.jaicore.testproblems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.testproblems.enhancedttsp.EnhancedTTSPNode;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class EnhancedTTSPGraphGenerator implements GraphGenerator<EnhancedTTSPNode, String>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(EnhancedTTSPGraphGenerator.class);

	private final EnhancedTTSP problem;

	public EnhancedTTSPGraphGenerator(final EnhancedTTSP problem) {
		super();
		this.problem = problem;
	}

	@Override
	public SingleRootGenerator<EnhancedTTSPNode> getRootGenerator() {
		return () -> this.problem.getInitalState();
	}

	@Override
	public SuccessorGenerator<EnhancedTTSPNode, String> getSuccessorGenerator() {
		return new SingleSuccessorGenerator<EnhancedTTSPNode, String>() {

			private ShortList getPossibleDestinationsThatHaveNotBeenGeneratedYet(final EnhancedTTSPNode n) {
				short curLoc = n.getCurLocation();
				ShortList possibleDestinationsToGoFromhere = new ShortArrayList();
				ShortList seenPlaces = n.getCurTour();
				int k = 0;
				boolean openPlaces = seenPlaces.size() < EnhancedTTSPGraphGenerator.this.problem.getPossibleDestinations().size() - 1;
				assert n.getCurTour().size() < EnhancedTTSPGraphGenerator.this.problem.getPossibleDestinations().size() : "We have already visited everything!";
				assert openPlaces || curLoc != 0 : "There are no open places (out of the " + EnhancedTTSPGraphGenerator.this.problem.getPossibleDestinations().size() + ", " + seenPlaces.size()
				+ " of which have already been seen) but we are still in the initial position. This smells like a strange TSP.";
				if (openPlaces) {
					for (short l : EnhancedTTSPGraphGenerator.this.problem.getPossibleDestinations()) {
						if (k++ == 0) {
							continue;
						}
						if (l != curLoc && !seenPlaces.contains(l)) {
							possibleDestinationsToGoFromhere.add(l);
						}
					}
				} else {
					possibleDestinationsToGoFromhere.add((short) 0);
				}
				return possibleDestinationsToGoFromhere;
			}

			@Override
			public List<NodeExpansionDescription<EnhancedTTSPNode, String>> generateSuccessors(final EnhancedTTSPNode node) throws InterruptedException {
				List<NodeExpansionDescription<EnhancedTTSPNode, String>> l = new ArrayList<>();
				if (node.getCurTour().size() >= EnhancedTTSPGraphGenerator.this.problem.getPossibleDestinations().size()) {
					EnhancedTTSPGraphGenerator.this.logger.warn("Cannot generate successors of a node in which we are in pos " + node.getCurLocation() + " and in which have already visited everything! " + (EnhancedTTSPGraphGenerator.this.getGoalTester().isGoal(node)
							? "The goal tester detects this as a goal, but the method is invoked nevertheless. Maybe the algorithm that uses this graph generator does not properly check the goal node property. Another possibility is that a goal check DIFFERENT from this one is used"
									: "The goal tester does not detect this as a goal node!"));
					return l;
				}
				ShortList possibleUntriedDestinations = this.getPossibleDestinationsThatHaveNotBeenGeneratedYet(node);
				if (possibleUntriedDestinations.contains(node.getCurLocation())) {
					throw new IllegalStateException("The list of possible destinations must not contain the current position " + node.getCurLocation() + ".");
				}
				int N = possibleUntriedDestinations.size();
				for (int i = 0; i < N; i++) {
					if (Thread.interrupted()) {
						throw new InterruptedException("Successor generation has been interrupted.");
					}
					l.add(this.generateSuccessor(node, possibleUntriedDestinations.getShort(i)));
				}
				return l;
			}

			public NodeExpansionDescription<EnhancedTTSPNode, String> generateSuccessor(final EnhancedTTSPNode n, final short destination) {
				return new NodeExpansionDescription<>(n, EnhancedTTSPGraphGenerator.this.problem.computeSuccessorState(n, destination), n.getCurLocation() + " -> " + destination, NodeType.OR);
			}

			@Override
			public NodeExpansionDescription<EnhancedTTSPNode, String> generateSuccessor(final EnhancedTTSPNode node, final int i) {
				ShortList availableDestinations = this.getPossibleDestinationsThatHaveNotBeenGeneratedYet(node);
				return this.generateSuccessor(node, availableDestinations.getShort(i % availableDestinations.size()));
			}

			@Override
			public boolean allSuccessorsComputed(final EnhancedTTSPNode node) {
				return this.getPossibleDestinationsThatHaveNotBeenGeneratedYet(node).isEmpty();
			}
		};
	}

	@Override
	public NodeGoalTester<EnhancedTTSPNode> getGoalTester() {
		return n -> {
			return n.getCurTour().size() >= this.problem.getPossibleDestinations().size() && n.getCurLocation() == this.problem.getStartLocation();
		};
	}

	@Override
	public boolean isSelfContained() {
		return true;
	}

	@Override
	public void setNodeNumbering(final boolean nodenumbering) {
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}
