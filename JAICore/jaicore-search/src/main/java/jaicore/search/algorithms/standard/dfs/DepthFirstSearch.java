package jaicore.search.algorithms.standard.dfs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.algorithms.standard.bestfirst.events.NodeExpansionCompletedEvent;
import jaicore.search.algorithms.standard.random.RandomSearch;
import jaicore.search.core.interfaces.AAnyPathInORGraphSearch;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;

/**
 *
 * @author fmohr
 *
 * @param <N>
 * @param <A>
 */
public class DepthFirstSearch<I extends GraphSearchInput<N, A>, N, A> extends AAnyPathInORGraphSearch<I, SearchGraphPath<N, A>, N, A> implements ILoggingCustomizable {

	/* logging */
	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(RandomSearch.class);

	private final List<N> currentPath = new ArrayList<>();
	private boolean lastNodeWasTrueLeaf = false;
	private Map<N, List<N>> successors = new HashMap<>();

	public DepthFirstSearch(final I problem) {
		super(problem);
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		try {
			checkAndConductTermination();
			registerActiveThread();
			logger.debug("Conducting step. Current path length is {}", currentPath.size());
			switch (getState()) {
			case created:
				N root = ((SingleRootGenerator<N>) getInput().getGraphGenerator().getRootGenerator()).getRoot();
				currentPath.add(root);
				post(new GraphInitializedEvent<>(getId(), root));
				logger.info("Algorithm activated.");
				return activate();
			case active:

				/* compute the currently relevant leaf */
				N leaf = currentPath.get(currentPath.size() - 1);
				if (lastNodeWasTrueLeaf) {
					currentPath.remove(leaf);
					N parent = currentPath.get(currentPath.size() - 1);
					logger.trace("Last node {} was a leaf node (goal or dead-end) in the original graph. Computing new leaf node by first switching to the next sibling of parent {}.", leaf, parent);
					int indexOfChildInSuccessorsOfParent = successors.get(parent).indexOf(leaf);
					assert indexOfChildInSuccessorsOfParent >= 0 : "Could not identify node " + leaf + " as a successor of " + parent + ". Successors of parent: " + successors.get(parent);
					logger.trace("Node {} is child #{} of the parent node {}.", leaf, indexOfChildInSuccessorsOfParent, parent);
					while (indexOfChildInSuccessorsOfParent == successors.get(parent).size() - 1) {
						logger.trace("Node {} is the last child of {}, so going one level up.", leaf, parent);
						successors.remove(parent);
						currentPath.get(currentPath.size() - 1);
						leaf = currentPath.get(currentPath.size() - 1);
						currentPath.remove(leaf);
						if (currentPath.isEmpty()) { // if this was the last node, terminate the algorithm
							return terminate();
						}
						parent = currentPath.get(currentPath.size() - 1);
						indexOfChildInSuccessorsOfParent = successors.get(parent).indexOf(leaf);
					}
					leaf = successors.get(parent).get(indexOfChildInSuccessorsOfParent + 1);
					currentPath.add(leaf);
					assert checkPathConsistency(currentPath);
				}
				logger.debug("Relevant leaf node is {}.", leaf);

				if (((NodeGoalTester<N>) getInput().getGraphGenerator().getGoalTester()).isGoal(leaf)) {
					lastNodeWasTrueLeaf = true;
					AlgorithmEvent event = new GraphSearchSolutionCandidateFoundEvent<>(this.getId(), new SearchGraphPath<>(currentPath));
					post(event);
					post(new NodeTypeSwitchEvent<>(getId(), leaf, "or_solution"));
					logger.debug("The leaf node is a goal node. Returning goal path {}", currentPath);
					return event;
				} else {
					logger.debug("The leaf node is not a goal node. Creating successors and diving into the first one.");
					post(new NodeTypeSwitchEvent<>(getId(), leaf, "or_closed"));
					final N expandedLeaf = leaf;
					List<N> successorsOfThis = computeTimeoutAware(() -> getInput().getGraphGenerator().getSuccessorGenerator().generateSuccessors(expandedLeaf).stream().map(NodeExpansionDescription::getTo).collect(Collectors.toList()));
					long lastTerminationCheck = 0;
					for (N child : successorsOfThis) {
						post(new NodeAddedEvent<>(getId(), expandedLeaf, child, "or_open"));
						if (System.currentTimeMillis() - lastTerminationCheck > 50) {
							checkAndConductTermination();
							lastTerminationCheck = System.currentTimeMillis();
						}
					}
					successors.put(leaf, successorsOfThis);
					lastNodeWasTrueLeaf = successorsOfThis.isEmpty();
					if (lastNodeWasTrueLeaf) {
						logger.debug("Detected that {} is a dead-end (has no successors and is not a goal node).", leaf);
					}
					else {
						currentPath.add(successorsOfThis.get(0));
						assert checkPathConsistency(currentPath);
						logger.debug("Computed {} successors for {}, and selected {} as the next successor. Current path is now {}.", successorsOfThis.size(), leaf, successorsOfThis.get(0), currentPath);
					}
					return new NodeExpansionCompletedEvent<>(getId(), leaf);
				}

			default:
				throw new IllegalStateException("Cannot do anything in state " + getState());
			}
		} finally {
			unregisterActiveThread();
		}
	}

	private boolean checkPathConsistency(List<N> path) {
		N last = null;
		for (N node : path) {
			if (last != null) {
				assert successors.containsKey(last) : "No successor entry found for node " + last;
				if (!successors.containsKey(last)) {
					return false;
				}
				boolean validEdge = successors.get(last).contains(node);
				assert validEdge : "The path has an edge from " + last + " to " + node + " that is not reflected in the successors.";
				if (!validEdge) {
					return false;
				}
			}
			last = node;
		}
		return true;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switch logger name from {} to {}", this.loggerName, name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(this.loggerName);
		if (this.getGraphGenerator() instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.getGraphGenerator()).setLoggerName(name + ".graphgen");
		}
		this.logger.info("Switched logger name to {}", this.loggerName);
		super.setLoggerName(this.loggerName + "._algorithm");
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}
}