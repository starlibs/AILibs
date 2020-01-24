package ai.libs.jaicore.search.testproblems.enhancedttsp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.ILazySuccessorGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.MappingIterator;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPState;
import ai.libs.jaicore.search.model.NodeExpansionDescription;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class EnhancedTTSPGraphGenerator implements IGraphGenerator<EnhancedTTSPState, String>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(EnhancedTTSPGraphGenerator.class);

	private final EnhancedTTSP problem;

	public EnhancedTTSPGraphGenerator(final EnhancedTTSP problem) {
		super();
		this.problem = problem;
	}

	@Override
	public ISingleRootGenerator<EnhancedTTSPState> getRootGenerator() {
		return () -> this.problem.getInitalState();
	}

	@Override
	public ISuccessorGenerator<EnhancedTTSPState, String> getSuccessorGenerator() {
		return new ILazySuccessorGenerator<EnhancedTTSPState, String>() {

			private ShortList getPossibleDestinationsThatHaveNotBeenGeneratedYet(final EnhancedTTSPState n) {
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
			public List<INewNodeDescription<EnhancedTTSPState, String>> generateSuccessors(final EnhancedTTSPState node) throws InterruptedException {
				List<INewNodeDescription<EnhancedTTSPState, String>> l = new ArrayList<>();
				if (node.getCurTour().size() >= EnhancedTTSPGraphGenerator.this.problem.getPossibleDestinations().size()) {
					EnhancedTTSPGraphGenerator.this.logger.warn("Cannot generate successors of a node in which we are in pos {} and in which have already visited everything!", node.getCurLocation());
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

			public NodeExpansionDescription<EnhancedTTSPState, String> generateSuccessor(final EnhancedTTSPState n, final short destination) {
				return new NodeExpansionDescription<>(EnhancedTTSPGraphGenerator.this.problem.computeSuccessorState(n, destination), n.getCurLocation() + " -> " + destination);
			}

			@Override
			public Iterator<INewNodeDescription<EnhancedTTSPState, String>> getIterativeGenerator(final EnhancedTTSPState node) {
				ShortList availableDestinations = this.getPossibleDestinationsThatHaveNotBeenGeneratedYet(node);
				return new MappingIterator<>(availableDestinations.iterator(), s -> this.generateSuccessor(node, s));
			}
		};
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
