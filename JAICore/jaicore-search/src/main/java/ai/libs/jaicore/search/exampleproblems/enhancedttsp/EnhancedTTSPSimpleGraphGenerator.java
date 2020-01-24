package ai.libs.jaicore.search.exampleproblems.enhancedttsp;

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
import it.unimi.dsi.fastutil.shorts.ShortList;

public class EnhancedTTSPSimpleGraphGenerator implements IGraphGenerator<EnhancedTTSPState, String>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(EnhancedTTSPSimpleGraphGenerator.class);

	private final EnhancedTTSP problem;

	public EnhancedTTSPSimpleGraphGenerator(final EnhancedTTSP problem) {
		super();
		this.problem = problem;
	}

	@Override
	public ISingleRootGenerator<EnhancedTTSPState> getRootGenerator() {
		return this.problem::getInitalState;
	}

	@Override
	public ISuccessorGenerator<EnhancedTTSPState, String> getSuccessorGenerator() {
		return new ILazySuccessorGenerator<EnhancedTTSPState, String>() {



			@Override
			public List<INewNodeDescription<EnhancedTTSPState, String>> generateSuccessors(final EnhancedTTSPState node) throws InterruptedException {
				List<INewNodeDescription<EnhancedTTSPState, String>> l = new ArrayList<>();
				if (node.getCurTour().size() >= EnhancedTTSPSimpleGraphGenerator.this.problem.getPossibleDestinations().size()) {
					EnhancedTTSPSimpleGraphGenerator.this.logger.warn("Cannot generate successors of a node in which we are in pos {} and in which have already visited everything!", node.getCurLocation());
					return l;
				}
				ShortList possibleUntriedDestinations = EnhancedTTSPSimpleGraphGenerator.this.problem.getPossibleRemainingDestinationsInState(node);
				if (possibleUntriedDestinations.contains(node.getCurLocation())) {
					throw new IllegalStateException("The list of possible destinations must not contain the current position " + node.getCurLocation() + ".");
				}
				int n = possibleUntriedDestinations.size();
				for (int i = 0; i < n; i++) {
					if (Thread.interrupted()) {
						throw new InterruptedException("Successor generation has been interrupted.");
					}
					l.add(this.generateSuccessor(node, possibleUntriedDestinations.getShort(i)));
				}
				return l;
			}

			public INewNodeDescription<EnhancedTTSPState, String> generateSuccessor(final EnhancedTTSPState n, final short destination) {
				return new NodeExpansionDescription<>(EnhancedTTSPSimpleGraphGenerator.this.problem.computeSuccessorState(n, destination), n.getCurLocation() + " -> " + destination);
			}

			@Override
			public Iterator<INewNodeDescription<EnhancedTTSPState, String>> getIterativeGenerator(final EnhancedTTSPState node) {
				ShortList availableDestinations = EnhancedTTSPSimpleGraphGenerator.this.problem.getPossibleRemainingDestinationsInState(node);
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
