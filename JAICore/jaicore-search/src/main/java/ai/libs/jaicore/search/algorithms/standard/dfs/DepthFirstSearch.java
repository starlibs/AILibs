package ai.libs.jaicore.search.algorithms.standard.dfs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.NodeExpansionCompletedEvent;
import ai.libs.jaicore.search.core.interfaces.AAnyPathInORGraphSearch;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

/**
 *
 * @author fmohr
 *
 * @param <N>
 * @param <A>
 */
public class DepthFirstSearch<N, A> extends AAnyPathInORGraphSearch<IPathSearchInput<N, A>, SearchGraphPath<N, A>, N, A> implements ILoggingCustomizable {

	/* logging */
	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(DepthFirstSearch.class);

	private final INodeGoalTester<N, A> goalTester;

	/* state of the algorithm */
	private SearchGraphPath<N, A> currentPath;
	private boolean lastNodeWasTrueLeaf = false;
	private Map<N, List<N>> successorsNodes = new HashMap<>();
	private Map<N, List<A>> successorsEdges = new HashMap<>();

	public DepthFirstSearch(final IPathSearchInput<N, A> problem) {
		super(problem);
		this.goalTester = (INodeGoalTester<N, A>)problem.getGoalTester();
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		try {
			this.checkAndConductTermination();
			this.registerActiveThread();
			this.logger.debug("Conducting step. Current path length is {}", this.currentPath != null ? this.currentPath.getNumberOfNodes() : "0");
			switch (this.getState()) {
			case CREATED:
				N root = ((ISingleRootGenerator<N>) this.getInput().getGraphGenerator().getRootGenerator()).getRoot();
				this.post(new GraphInitializedEvent<>(this, root));

				/* check whether a path has already been set externally */
				if (this.currentPath == null) {
					this.currentPath = new SearchGraphPath<>(this.getInput().getGraphGenerator().getRootGenerator().getRoots().iterator().next());
				} else {
					if (!this.currentPath.getRoot().equals(root)) {
						throw new IllegalArgumentException("The root of the given path is not the root of the tree provided by the graph generator.");
					}

					/* post an event of all the nodes that have been added */
					int n = this.currentPath.getNumberOfNodes();
					for (int i = 0; i < n - 1; i++) {
						N node = this.currentPath.getNodes().get(i);
						for (N successor : this.successorsNodes.get(node)) {
							this.post(new NodeAddedEvent<>(this, node, successor, "or_open"));
							this.post(new NodeTypeSwitchEvent<>(this, node, "or_closed"));
						}
					}
					N leaf = this.currentPath.getHead();
					if (this.goalTester.isGoal(leaf)) {
						this.post(new NodeTypeSwitchEvent<>(this, this.currentPath.getHead(), "or_solution"));
						this.lastNodeWasTrueLeaf = true;
					} else if (this.getInput().getGraphGenerator().getSuccessorGenerator().generateSuccessors(leaf).isEmpty()) {
						this.lastNodeWasTrueLeaf = true;
					}
				}

				this.logger.info("Algorithm activated.");
				return this.activate();
			case ACTIVE:

				/* compute the currently relevant leaf */
				N leaf = this.currentPath.getHead();
				if (this.lastNodeWasTrueLeaf) {

					/* find the deepest antecessor of the leaf that has not been exhausted */
					N formerLeaf;
					int indexOfChildInSuccessorsOfParent;
					do {
						if (this.currentPath.getNumberOfNodes() == 1) { // if this was the last node, terminate the algorithm
							return this.terminate();
						}
						this.successorsNodes.remove(leaf); // this is relevant if the leaf is actually now an inner node
						this.currentPath.cutHead();
						formerLeaf = leaf;
						this.logger.trace("Last node {} was a leaf node (goal or dead-end) in the original graph. Computing new leaf node by first switching to the next sibling of parent {}.", formerLeaf, leaf);
						leaf = this.currentPath.getHead();
						indexOfChildInSuccessorsOfParent = this.successorsNodes.get(leaf).indexOf(formerLeaf);
						assert indexOfChildInSuccessorsOfParent >= 0 : "Could not identify node " + leaf + " as a successor of " + leaf + ". Successors of parent: " + this.successorsNodes.get(leaf);
						this.logger.trace("Node {} is child #{} of the parent node {}.", formerLeaf, indexOfChildInSuccessorsOfParent, leaf);
					}
					while (indexOfChildInSuccessorsOfParent == this.successorsNodes.get(leaf).size() - 1);

					/* get next successor of the current leaf */
					A successorAction = this.successorsEdges.get(leaf).get(indexOfChildInSuccessorsOfParent + 1);
					leaf = this.successorsNodes.get(leaf).get(indexOfChildInSuccessorsOfParent + 1);
					this.currentPath.extend(leaf, successorAction);
					assert this.checkPathConsistency(this.currentPath);
				}
				this.logger.debug("Relevant leaf node is {}.", leaf);

				if (this.goalTester.isGoal(leaf)) {
					this.lastNodeWasTrueLeaf = true;
					IAlgorithmEvent event = new GraphSearchSolutionCandidateFoundEvent<>(this, new SearchGraphPath<N, A>(this.currentPath));
					this.post(event);
					this.post(new NodeTypeSwitchEvent<>(this, leaf, "or_solution"));
					this.logger.debug("The leaf node is a goal node. Returning goal path {}", this.currentPath);
					return event;
				} else {
					this.logger.debug("The leaf node is not a goal node. Creating successors and diving into the first one.");
					this.post(new NodeTypeSwitchEvent<>(this, leaf, "or_closed"));
					final N expandedLeaf = leaf;
					List<INewNodeDescription<N, A>> successorsOfThis = this.computeTimeoutAware(
							() -> this.getInput().getGraphGenerator().getSuccessorGenerator().generateSuccessors(expandedLeaf), "DFS successor generation", true);
					long lastTerminationCheck = 0;
					List<N> successorNodes = new ArrayList<>();
					List<A> successorEdges = new ArrayList<>();
					for (INewNodeDescription<N, A> child : successorsOfThis) {
						this.post(new NodeAddedEvent<>(this, expandedLeaf, child.getTo(), "or_open"));
						if (System.currentTimeMillis() - lastTerminationCheck > 50) {
							this.checkAndConductTermination();
							lastTerminationCheck = System.currentTimeMillis();
						}
						successorNodes.add(child.getTo());
						successorEdges.add(child.getArcLabel());
					}
					this.successorsNodes.put(leaf, successorNodes);
					this.successorsEdges.put(leaf, successorEdges);
					this.lastNodeWasTrueLeaf = successorsOfThis.isEmpty();
					if (this.lastNodeWasTrueLeaf) {
						this.logger.debug("Detected that {} is a dead-end (has no successors and is not a goal node).", leaf);
					} else {
						this.currentPath.extend(successorNodes.get(0), successorEdges.get(0));
						assert this.checkPathConsistency(this.currentPath);
						this.logger.debug("Computed {} successors for {}, and selected {} as the next successor. Current path is now {}.", successorsOfThis.size(), leaf, successorsOfThis.get(0), this.currentPath);
					}
					return new NodeExpansionCompletedEvent<>(this, leaf);
				}

			default:
				throw new IllegalStateException("Cannot do anything in state " + this.getState());
			}
		} finally {
			this.unregisterActiveThread();
		}
	}

	public ILabeledPath<N, A> getCurrentPath() {
		return this.currentPath.getUnmodifiableAccessor();
	}

	public int[] getDecisionIndicesForCurrentPath() {
		int n = this.currentPath.getNumberOfNodes();
		int[] decisions = new int[n - 1];
		ILabeledPath<N, A> tmpPath = this.getCurrentPath();
		N last = null;
		for (int i = 0; i < n; i++) {
			N current = tmpPath.getRoot();
			if (last != null) {
				decisions[i - 1] = this.successorsNodes.get(last).indexOf(current);
				assert decisions[i - 1] != -1;
			}
			last = current;
			tmpPath = tmpPath.getPathFromChildOfRoot();
		}
		return decisions;
	}

	public void setCurrentPath(final ILabeledPath<N, A> path) {
		try {

			/* check that the root of the path is consistent with the true root */
			Object root = this.currentPath.getNumberOfNodes() == 0 ? ((ISingleRootGenerator<?>) this.getGraphGenerator().getRootGenerator()).getRoot() : this.currentPath.getNodes().get(0);
			if (!root.equals(path.getRoot())) {
				throw new IllegalArgumentException();
			}

			/* now check that all other nodes are also valid successors in the original graph */
			Map<N, List<N>> tentativeSuccessors = new HashMap<>();
			ISuccessorGenerator<N, A> successorGenerator = this.getGraphGenerator().getSuccessorGenerator();
			int n = path.getNumberOfNodes();
			for (int i = 0; i < n; i++) {
				N node = path.getNodes().get(i);
				if (i > 0 && !tentativeSuccessors.get(path.getNodes().get(i - 1)).contains(node)) {
					throw new IllegalArgumentException("Node " + node + " is not a successor of " + path.getNodes().get(i - 1) + " in the original graph.");
				}
				if (i < n - 1) {
					tentativeSuccessors.put(node, successorGenerator.generateSuccessors(node).stream().map(INewNodeDescription::getTo).collect(Collectors.toList()));
				}
			}

			/* replace successor map and current path variable */
			this.currentPath = new SearchGraphPath<>(path);
			this.successorsNodes.clear();
			this.successorsNodes.putAll(tentativeSuccessors);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void setCurrentPath(final int... decisions) {
		try {
			N root = this.currentPath.getNumberOfNodes() == 0 ? ((ISingleRootGenerator<N>) this.getGraphGenerator().getRootGenerator()).getRoot() : this.currentPath.getRoot();
			SearchGraphPath<N, A> tentativePath = new SearchGraphPath<>(root);
			Map<N, List<N>> tentativeSuccessors = new HashMap<>();
			Map<N, List<A>> tentativeSuccessorActions = new HashMap<>();
			ISuccessorGenerator<N, A> successorGenerator = this.getGraphGenerator().getSuccessorGenerator();
			int n = decisions.length;
			for (int i = 0; i < n; i++) {
				N node = tentativePath.getHead();
				List<INewNodeDescription<N, A>> descriptions = successorGenerator.generateSuccessors(node);
				tentativeSuccessors.put(node, descriptions.stream().map(INewNodeDescription::getTo).collect(Collectors.toList()));
				tentativeSuccessorActions.put(node, descriptions.stream().map(INewNodeDescription::getArcLabel).collect(Collectors.toList()));
				tentativePath.extend(tentativeSuccessors.get(node).get(decisions[i]), tentativeSuccessorActions.get(node).get(decisions[i]));
			}

			/* replace successor map and current path variable */
			this.currentPath = tentativePath;
			this.successorsNodes.clear();
			this.successorsNodes.putAll(tentativeSuccessors);
			this.checkPathConsistency(this.currentPath);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private boolean checkPathConsistency(final ILabeledPath<N, A> path) {
		N last = null;
		for (N node : path.getNodes()) {
			if (last != null) {
				assert this.successorsNodes.containsKey(last) : "No successor entry found for node " + last;
				if (!this.successorsNodes.containsKey(last)) {
					return false;
				}
				if (!this.successorsNodes.get(last).contains(node)) {
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