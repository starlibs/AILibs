package jaicore.search.algorithms.standard.dfs;

import java.util.ArrayList;
import java.util.Collections;
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
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

/**
 *
 * @author fmohr
 *
 * @param <N>
 * @param <A>
 */
public class DepthFirstSearch<N, A> extends AAnyPathInORGraphSearch<GraphSearchInput<N, A>, SearchGraphPath<N, A>, N, A> implements ILoggingCustomizable {

	/* logging */
	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(RandomSearch.class);
	
	/* state of the algorithm */
	private final List<N> currentPath = new ArrayList<>();
	private boolean lastNodeWasTrueLeaf = false;
	private Map<N, List<N>> successors = new HashMap<>();

	public DepthFirstSearch(final GraphSearchInput<N, A> problem) {
		super(problem);
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		try {
			checkAndConductTermination();
			registerActiveThread();
			logger.debug("Conducting step. Current path length is {}", currentPath.size());
			switch (getState()) {
			case CREATED:
				N root = ((SingleRootGenerator<N>) getInput().getGraphGenerator().getRootGenerator()).getRoot();
				post(new GraphInitializedEvent<>(getId(), root));

				/* check whether a path has already been set externally */
				if (currentPath.isEmpty()) {
					currentPath.add(root);
				} else {
					if (!currentPath.get(0).equals(root))
						throw new IllegalArgumentException("The root of the given path is not the root of the tree provided by the graph generator.");

					/* post an event of all the nodes that have been added */
					int n = currentPath.size();
					for (int i = 0; i < n - 1; i++) {
						N node = currentPath.get(i);
						for (N successor : successors.get(node)) {
							post(new NodeAddedEvent<>(getId(), node, successor, "or_open"));
							post(new NodeTypeSwitchEvent<>(getId(), node, "or_closed"));
						}
					}
					N leaf = currentPath.get(n - 1);
					if (((NodeGoalTester<N>) getInput().getGraphGenerator().getGoalTester()).isGoal(leaf)) {
						post(new NodeTypeSwitchEvent<>(getId(), currentPath.get(n - 1), "or_solution"));
						lastNodeWasTrueLeaf = true;
					}
					else if (getInput().getGraphGenerator().getSuccessorGenerator().generateSuccessors(leaf).isEmpty()) {
						lastNodeWasTrueLeaf = true;
					}
				}

				logger.info("Algorithm activated.");
				return activate();
			case ACTIVE:

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
					AlgorithmEvent event = new GraphSearchSolutionCandidateFoundEvent<>(getId(), new SearchGraphPath<>(currentPath));
					post(event);
					post(new NodeTypeSwitchEvent<>(getId(), leaf, "or_solution"));
					logger.debug("The leaf node is a goal node. Returning goal path {}", currentPath);
					return event;
				} else {
					logger.debug("The leaf node is not a goal node. Creating successors and diving into the first one.");
					post(new NodeTypeSwitchEvent<>(getId(), leaf, "or_closed"));
					final N expandedLeaf = leaf;
					List<N> successorsOfThis = computeTimeoutAware(() -> getInput().getGraphGenerator().getSuccessorGenerator().generateSuccessors(expandedLeaf).stream().map(NodeExpansionDescription::getTo).collect(Collectors.toList()), "DFS successor generation", true);
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
					} else {
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

	public List<N> getCurrentPath() {
		return Collections.unmodifiableList(currentPath);
	}

	public int[] getDecisionIndicesForCurrentPath() {
		int n = currentPath.size();
		int[] decisions = new int[n - 1];
		for (int i = 1; i < n; i++) {
			N parent = currentPath.get(i - 1);
			decisions[i - 1] = successors.get(parent).indexOf(currentPath.get(i));
			assert decisions[i - 1] != -1;
		}
		return decisions;
	}

	public void setCurrentPath(final List<N> path) {
		try {

			/* check that the root of the path is consistent with the true root */
			Object root = currentPath.isEmpty() ? ((SingleRootGenerator<?>) getGraphGenerator().getRootGenerator()).getRoot() : currentPath.get(0);
			if (!root.equals(path.get(0)))
				throw new IllegalArgumentException();

			/* now check that all other nodes are also valid successors in the original graph */
			Map<N, List<N>> tentativeSuccessors = new HashMap<>();
			SuccessorGenerator<N, A> successorGenerator = getGraphGenerator().getSuccessorGenerator();
			int n = path.size();
			for (int i = 0; i < n; i++) {
				N node = path.get(i);
				if (i > 0 && !tentativeSuccessors.get(path.get(i - 1)).contains(node))
					throw new IllegalArgumentException("Node " + node + " is not a successor of " + path.get(i - 1) + " in the original graph.");
				if (i < n - 1) {
					tentativeSuccessors.put(node, successorGenerator.generateSuccessors(node).stream().map(NodeExpansionDescription::getTo).collect(Collectors.toList()));
				}
			}

			/* replace successor map and current path variable */
			this.currentPath.clear();
			this.currentPath.addAll(path);
			successors.clear();
			successors.putAll(tentativeSuccessors);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void setCurrentPath(int... decisions) {
		try {
			N root = currentPath.isEmpty() ? ((SingleRootGenerator<N>) getGraphGenerator().getRootGenerator()).getRoot() : currentPath.get(0);
			List<N> tentativePath = new ArrayList<>();
			tentativePath.add(root);
			Map<N, List<N>> tentativeSuccessors = new HashMap<>();
			SuccessorGenerator<N, A> successorGenerator = getGraphGenerator().getSuccessorGenerator();
			int n = decisions.length;
			for (int i = 0; i < n; i++) {
				N node = tentativePath.get(i);
				tentativeSuccessors.put(node, successorGenerator.generateSuccessors(node).stream().map(NodeExpansionDescription::getTo).collect(Collectors.toList()));
				tentativePath.add(tentativeSuccessors.get(node).get(decisions[i]));
			}

			/* replace successor map and current path variable */
			this.currentPath.clear();
			this.currentPath.addAll(tentativePath);
			successors.clear();
			successors.putAll(tentativeSuccessors);
			checkPathConsistency(currentPath);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private boolean checkPathConsistency(final List<N> path) {
		N last = null;
		for (N node : path) {
			if (last != null) {
				assert successors.containsKey(last) : "No successor entry found for node " + last;
				if (!successors.containsKey(last)) {
					return false;
				}
				if (!successors.get(last).contains(node)) {
					throw new IllegalStateException("The path has an edge from " + last + " to " + node + " that is not reflected in the successors.");
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
		if (getGraphGenerator() instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) getGraphGenerator()).setLoggerName(name + ".graphgen");
		}
		this.logger.info("Switched logger name to {}", this.loggerName);
		super.setLoggerName(this.loggerName + "._algorithm");
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}
}