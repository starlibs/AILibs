package ai.libs.jaicore.search.algorithms.standard.dfs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeExpansionDescription;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeGoalTester;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.SingleRootGenerator;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.SuccessorGenerator;
import org.api4.java.algorithm.events.AlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.NodeExpansionCompletedEvent;
import ai.libs.jaicore.search.algorithms.standard.random.RandomSearch;
import ai.libs.jaicore.search.core.interfaces.AAnyPathInORGraphSearch;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

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
			this.checkAndConductTermination();
			this.registerActiveThread();
			this.logger.debug("Conducting step. Current path length is {}", this.currentPath.size());
			switch (this.getState()) {
			case CREATED:
				N root = ((SingleRootGenerator<N>) this.getInput().getGraphGenerator().getRootGenerator()).getRoot();
				this.post(new GraphInitializedEvent<>(this.getId(), root));

				/* check whether a path has already been set externally */
				if (this.currentPath.isEmpty()) {
					this.currentPath.add(root);
				} else {
					if (!this.currentPath.get(0).equals(root)) {
						throw new IllegalArgumentException("The root of the given path is not the root of the tree provided by the graph generator.");
					}

					/* post an event of all the nodes that have been added */
					int n = this.currentPath.size();
					for (int i = 0; i < n - 1; i++) {
						N node = this.currentPath.get(i);
						for (N successor : this.successors.get(node)) {
							this.post(new NodeAddedEvent<>(this.getId(), node, successor, "or_open"));
							this.post(new NodeTypeSwitchEvent<>(this.getId(), node, "or_closed"));
						}
					}
					N leaf = this.currentPath.get(n - 1);
					if (((NodeGoalTester<N, A>) this.getInput().getGraphGenerator().getGoalTester()).isGoal(leaf)) {
						this.post(new NodeTypeSwitchEvent<>(this.getId(), this.currentPath.get(n - 1), "or_solution"));
						this.lastNodeWasTrueLeaf = true;
					} else if (this.getInput().getGraphGenerator().getSuccessorGenerator().generateSuccessors(leaf).isEmpty()) {
						this.lastNodeWasTrueLeaf = true;
					}
				}

				this.logger.info("Algorithm activated.");
				return this.activate();
			case ACTIVE:

				/* compute the currently relevant leaf */
				N leaf = this.currentPath.get(this.currentPath.size() - 1);
				if (this.lastNodeWasTrueLeaf) {
					this.currentPath.remove(leaf);
					N parent = this.currentPath.get(this.currentPath.size() - 1);
					this.logger.trace("Last node {} was a leaf node (goal or dead-end) in the original graph. Computing new leaf node by first switching to the next sibling of parent {}.", leaf, parent);
					int indexOfChildInSuccessorsOfParent = this.successors.get(parent).indexOf(leaf);
					assert indexOfChildInSuccessorsOfParent >= 0 : "Could not identify node " + leaf + " as a successor of " + parent + ". Successors of parent: " + this.successors.get(parent);
					this.logger.trace("Node {} is child #{} of the parent node {}.", leaf, indexOfChildInSuccessorsOfParent, parent);
					while (indexOfChildInSuccessorsOfParent == this.successors.get(parent).size() - 1) {
						this.logger.trace("Node {} is the last child of {}, so going one level up.", leaf, parent);
						this.successors.remove(parent);
						this.currentPath.get(this.currentPath.size() - 1);
						leaf = this.currentPath.get(this.currentPath.size() - 1);
						this.currentPath.remove(leaf);
						if (this.currentPath.isEmpty()) { // if this was the last node, terminate the algorithm
							return this.terminate();
						}
						parent = this.currentPath.get(this.currentPath.size() - 1);
						indexOfChildInSuccessorsOfParent = this.successors.get(parent).indexOf(leaf);
					}
					leaf = this.successors.get(parent).get(indexOfChildInSuccessorsOfParent + 1);
					this.currentPath.add(leaf);
					assert this.checkPathConsistency(this.currentPath);
				}
				this.logger.debug("Relevant leaf node is {}.", leaf);

				if (((NodeGoalTester<N, A>) this.getInput().getGraphGenerator().getGoalTester()).isGoal(leaf)) {
					this.lastNodeWasTrueLeaf = true;
					AlgorithmEvent event = new GraphSearchSolutionCandidateFoundEvent<>(this.getId(), new SearchGraphPath<>(this.currentPath));
					this.post(event);
					this.post(new NodeTypeSwitchEvent<>(this.getId(), leaf, "or_solution"));
					this.logger.debug("The leaf node is a goal node. Returning goal path {}", this.currentPath);
					return event;
				} else {
					this.logger.debug("The leaf node is not a goal node. Creating successors and diving into the first one.");
					this.post(new NodeTypeSwitchEvent<>(this.getId(), leaf, "or_closed"));
					final N expandedLeaf = leaf;
					List<N> successorsOfThis = this.computeTimeoutAware(
							() -> this.getInput().getGraphGenerator().getSuccessorGenerator().generateSuccessors(expandedLeaf).stream().map(NodeExpansionDescription::getTo).collect(Collectors.toList()), "DFS successor generation", true);
					long lastTerminationCheck = 0;
					for (N child : successorsOfThis) {
						this.post(new NodeAddedEvent<>(this.getId(), expandedLeaf, child, "or_open"));
						if (System.currentTimeMillis() - lastTerminationCheck > 50) {
							this.checkAndConductTermination();
							lastTerminationCheck = System.currentTimeMillis();
						}
					}
					this.successors.put(leaf, successorsOfThis);
					this.lastNodeWasTrueLeaf = successorsOfThis.isEmpty();
					if (this.lastNodeWasTrueLeaf) {
						this.logger.debug("Detected that {} is a dead-end (has no successors and is not a goal node).", leaf);
					} else {
						this.currentPath.add(successorsOfThis.get(0));
						assert this.checkPathConsistency(this.currentPath);
						this.logger.debug("Computed {} successors for {}, and selected {} as the next successor. Current path is now {}.", successorsOfThis.size(), leaf, successorsOfThis.get(0), this.currentPath);
					}
					return new NodeExpansionCompletedEvent<>(this.getId(), leaf);
				}

			default:
				throw new IllegalStateException("Cannot do anything in state " + this.getState());
			}
		} finally {
			this.unregisterActiveThread();
		}
	}

	public List<N> getCurrentPath() {
		return Collections.unmodifiableList(this.currentPath);
	}

	public int[] getDecisionIndicesForCurrentPath() {
		int n = this.currentPath.size();
		int[] decisions = new int[n - 1];
		for (int i = 1; i < n; i++) {
			N parent = this.currentPath.get(i - 1);
			decisions[i - 1] = this.successors.get(parent).indexOf(this.currentPath.get(i));
			assert decisions[i - 1] != -1;
		}
		return decisions;
	}

	public void setCurrentPath(final List<N> path) {
		try {

			/* check that the root of the path is consistent with the true root */
			Object root = this.currentPath.isEmpty() ? ((SingleRootGenerator<?>) this.getGraphGenerator().getRootGenerator()).getRoot() : this.currentPath.get(0);
			if (!root.equals(path.get(0))) {
				throw new IllegalArgumentException();
			}

			/* now check that all other nodes are also valid successors in the original graph */
			Map<N, List<N>> tentativeSuccessors = new HashMap<>();
			SuccessorGenerator<N, A> successorGenerator = this.getGraphGenerator().getSuccessorGenerator();
			int n = path.size();
			for (int i = 0; i < n; i++) {
				N node = path.get(i);
				if (i > 0 && !tentativeSuccessors.get(path.get(i - 1)).contains(node)) {
					throw new IllegalArgumentException("Node " + node + " is not a successor of " + path.get(i - 1) + " in the original graph.");
				}
				if (i < n - 1) {
					tentativeSuccessors.put(node, successorGenerator.generateSuccessors(node).stream().map(NodeExpansionDescription::getTo).collect(Collectors.toList()));
				}
			}

			/* replace successor map and current path variable */
			this.currentPath.clear();
			this.currentPath.addAll(path);
			this.successors.clear();
			this.successors.putAll(tentativeSuccessors);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void setCurrentPath(final int... decisions) {
		try {
			N root = this.currentPath.isEmpty() ? ((SingleRootGenerator<N>) this.getGraphGenerator().getRootGenerator()).getRoot() : this.currentPath.get(0);
			List<N> tentativePath = new ArrayList<>();
			tentativePath.add(root);
			Map<N, List<N>> tentativeSuccessors = new HashMap<>();
			SuccessorGenerator<N, A> successorGenerator = this.getGraphGenerator().getSuccessorGenerator();
			int n = decisions.length;
			for (int i = 0; i < n; i++) {
				N node = tentativePath.get(i);
				tentativeSuccessors.put(node, successorGenerator.generateSuccessors(node).stream().map(NodeExpansionDescription::getTo).collect(Collectors.toList()));
				tentativePath.add(tentativeSuccessors.get(node).get(decisions[i]));
			}

			/* replace successor map and current path variable */
			this.currentPath.clear();
			this.currentPath.addAll(tentativePath);
			this.successors.clear();
			this.successors.putAll(tentativeSuccessors);
			this.checkPathConsistency(this.currentPath);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private boolean checkPathConsistency(final List<N> path) {
		N last = null;
		for (N node : path) {
			if (last != null) {
				assert this.successors.containsKey(last) : "No successor entry found for node " + last;
				if (!this.successors.containsKey(last)) {
					return false;
				}
				if (!this.successors.get(last).contains(node)) {
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