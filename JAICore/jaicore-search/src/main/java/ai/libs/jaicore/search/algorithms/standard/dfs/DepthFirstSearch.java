package ai.libs.jaicore.search.algorithms.standard.dfs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.api4.java.ai.graphsearch.problem.IGraphSearchInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.PathGoalTester;
import org.api4.java.algorithm.events.AlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.IPath;
import org.api4.java.datastructure.graph.implicit.NodeExpansionDescription;
import org.api4.java.datastructure.graph.implicit.SingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.SuccessorGenerator;
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

/**
 *
 * @author fmohr
 *
 * @param <N>
 * @param <A>
 */
public class DepthFirstSearch<N, A> extends AAnyPathInORGraphSearch<IGraphSearchInput<N, A>, SearchGraphPath<N, A>, N, A> implements ILoggingCustomizable {

	/* logging */
	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(RandomSearch.class);

	private final PathGoalTester<N, A> goalTester;

	/* state of the algorithm */
	private final List<N> currentPathNodes = new ArrayList<>();
	private final List<A> currentPathEdges = new ArrayList<>();
	private boolean lastNodeWasTrueLeaf = false;
	private Map<N, List<NodeExpansionDescription<N, A>>> successors = new HashMap<>();

	public DepthFirstSearch(final IGraphSearchInput<N, A> problem) {
		super(problem);
		this.goalTester = problem.getGoalTester();
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		try {
			this.checkAndConductTermination();
			this.checkCurrentPathConsistency();
			this.registerActiveThread();
			this.logger.debug("Conducting step. Current path length is {}", this.currentPathNodes.size());
			switch (this.getState()) {
			case CREATED:
				N root = ((SingleRootGenerator<N>) this.getInput().getGraphGenerator().getRootGenerator()).getRoot();
				this.post(new GraphInitializedEvent<>(this.getId(), root));

				/* check whether a path has already been set externally */
				if (this.currentPathNodes.isEmpty()) {
					this.currentPathNodes.add(root);
				} else {
					if (!this.currentPathNodes.get(0).equals(root)) {
						throw new IllegalArgumentException("The root of the given path is not the root of the tree provided by the graph generator.");
					}

					/* post an event of all the nodes that have been added */
					int n = this.currentPathNodes.size();
					for (int i = 0; i < n - 1; i++) {
						N node = this.currentPathNodes.get(i);
						for (NodeExpansionDescription<N, A> successor : this.successors.get(node)) {
							this.post(new NodeAddedEvent<>(this.getId(), node, successor.getTo(), "or_open"));
							this.post(new NodeTypeSwitchEvent<>(this.getId(), node, "or_closed"));
						}
					}
					N leaf = this.currentPathNodes.get(n - 1);
					if (this.goalTester.isGoal(this.getCurrentPathAsIPath())) {
						this.post(new NodeTypeSwitchEvent<>(this.getId(), this.currentPathNodes.get(n - 1), "or_solution"));
						this.lastNodeWasTrueLeaf = true;
					} else if (this.getInput().getGraphGenerator().getSuccessorGenerator().generateSuccessors(leaf).isEmpty()) {
						this.lastNodeWasTrueLeaf = true;
					}
				}

				this.logger.info("Algorithm activated.");
				return this.activate();
			case ACTIVE:

				this.checkCurrentPathConsistency();

				/* compute the currently relevant leaf */
				N leaf = this.currentPathNodes.get(this.currentPathNodes.size() - 1);

				/* if the last node was a leaf, move to next sibling (next child of parent) */
				this.checkCurrentPathConsistency();
				if (this.lastNodeWasTrueLeaf) {
					this.currentPathNodes.remove(leaf);
					if (!this.currentPathEdges.isEmpty()) {
						this.currentPathEdges.remove(this.currentPathEdges.size() - 1);
					}
					N parent = this.currentPathNodes.get(this.currentPathNodes.size() - 1);
					this.logger.trace("Last node {} was a leaf node (goal or dead-end) in the original graph. Computing new leaf node by first switching to the next sibling of parent {}.", leaf, parent);
					int indexOfChildInSuccessorsOfParent = this.getChildIndex(parent, leaf);
					assert indexOfChildInSuccessorsOfParent >= 0 : "Could not identify node " + leaf + " as a successor of " + parent + ". Successors of parent: " + this.successors.get(parent);
					this.logger.trace("Node {} is child #{} of the parent node {}.", leaf, indexOfChildInSuccessorsOfParent, parent);
					this.checkCurrentPathConsistency();
					while (indexOfChildInSuccessorsOfParent == this.successors.get(parent).size() - 1) {
						this.logger.trace("Node {} is the last child of {}, so going one level up.", leaf, parent);
						this.successors.remove(parent);
						this.currentPathNodes.get(this.currentPathNodes.size() - 1);
						leaf = this.currentPathNodes.get(this.currentPathNodes.size() - 1);
						this.currentPathNodes.remove(leaf);
						if (!this.currentPathEdges.isEmpty()) {
							this.currentPathEdges.remove(this.currentPathEdges.size() - 1);
						}
						if (this.currentPathNodes.isEmpty()) { // if this was the last node, terminate the algorithm
							return this.terminate();
						}
						parent = this.currentPathNodes.get(this.currentPathNodes.size() - 1);
						indexOfChildInSuccessorsOfParent = this.getChildIndex(parent, leaf);
						this.checkCurrentPathConsistency();
					}
					leaf = this.successors.get(parent).get(indexOfChildInSuccessorsOfParent + 1).getTo();
					this.currentPathNodes.add(leaf);
					this.currentPathEdges.add(this.successors.get(parent).get(indexOfChildInSuccessorsOfParent + 1).getAction());
					this.checkCurrentPathConsistency();
				}
				this.checkCurrentPathConsistency();
				this.logger.debug("Relevant leaf node is {}.", leaf);

				if (this.goalTester.isGoal(this.getCurrentPathAsIPath())) {
					this.lastNodeWasTrueLeaf = true;
					AlgorithmEvent event = new GraphSearchSolutionCandidateFoundEvent<>(this.getId(), this.getCurrentPathAsIPath());
					this.post(event);
					this.post(new NodeTypeSwitchEvent<>(this.getId(), leaf, "or_solution"));
					this.logger.debug("The leaf node is a goal node. Returning goal path {}", this.currentPathNodes);
					return event;
				} else {
					this.logger.debug("The leaf node is not a goal node. Creating successors and diving into the first one.");
					this.post(new NodeTypeSwitchEvent<>(this.getId(), leaf, "or_closed"));
					final N expandedLeaf = leaf;
					List<NodeExpansionDescription<N, A>> successorsOfThis;
					try {
						successorsOfThis = this.computeTimeoutAware(
								() -> this.getInput().getGraphGenerator().getSuccessorGenerator().generateSuccessors(expandedLeaf), "DFS successor generation", true);
					} catch (ExecutionException e) {
						throw new AlgorithmException("Could not compute successors", e.getCause());
					}
					long lastTerminationCheck = 0;
					for (NodeExpansionDescription<N, A> child : successorsOfThis) {
						this.post(new NodeAddedEvent<>(this.getId(), expandedLeaf, child.getTo(), "or_open"));
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
						this.currentPathNodes.add(successorsOfThis.get(0).getTo());
						this.currentPathEdges.add(successorsOfThis.get(0).getAction());
						assert this.checkPathConsistency(this.currentPathNodes);
						this.logger.debug("Computed {} successors for {}, and selected {} as the next successor. Current path is now {}.", successorsOfThis.size(), leaf, successorsOfThis.get(0), this.currentPathNodes);
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

	public SearchGraphPath<N, A> getCurrentPathAsIPath() {
		return new SearchGraphPath<>(this.currentPathNodes, this.currentPathEdges);
	}

	private void checkCurrentPathConsistency() {
		assert (this.currentPathEdges.isEmpty() && this.currentPathNodes.size() <= 1 || this.currentPathEdges.size() == this.currentPathNodes.size() - 1) : "Have " + this.currentPathNodes.size() + " nodes and " + this.currentPathEdges.size() + " edges.\nNodes: \n\t" + this.currentPathNodes.stream().map(Object::toString).collect(Collectors.joining("\n\t")) + "\nEdges: \n\t" + this.currentPathEdges.stream().map(Object::toString).collect(Collectors.joining("\n\t"));
	}

	public List<N> getCurrentPath() {
		return Collections.unmodifiableList(this.currentPathNodes);
	}

	public int getChildIndex(final N from, final N to) {
		List<NodeExpansionDescription<N, A>> successorsOfNode = this.successors.get(from);
		for (int i = 0; i < successorsOfNode.size(); i++) {
			if (successorsOfNode.get(i).getTo().equals(to)) {
				return i;
			}
		}
		return -1;
	}

	public int[] getDecisionIndicesForCurrentPath() {
		int n = this.currentPathNodes.size();
		int[] decisions = new int[n - 1];
		for (int i = 1; i < n; i++) {
			N parent = this.currentPathNodes.get(i - 1);
			decisions[i - 1] = this.getChildIndex(parent, this.currentPathNodes.get(i));
			assert decisions[i - 1] != -1;
		}
		return decisions;
	}

	public void setCurrentPath(final IPath<N, A> path) {
		try {

			List<N> nodes = path.getNodes();
			List<A> edges = path.getArcs();

			/* check that the root of the path is consistent with the true root */
			Object root = this.currentPathNodes.isEmpty() ? ((SingleRootGenerator<?>) this.getGraphGenerator().getRootGenerator()).getRoot() : this.currentPathNodes.get(0);
			if (!root.equals(nodes.get(0))) {
				throw new IllegalArgumentException();
			}

			/* now check that all other nodes are also valid successors in the original graph */
			Map<N, List<NodeExpansionDescription<N, A>>> tentativeSuccessors = new HashMap<>();
			SuccessorGenerator<N, A> successorGenerator = this.getGraphGenerator().getSuccessorGenerator();
			int n = nodes.size();
			for (int i = 0; i < n; i++) {
				N node = nodes.get(i);
				if (i > 0 && tentativeSuccessors.get(nodes.get(i - 1)).stream().noneMatch(nd -> nd.getTo().equals(node))) {
					throw new IllegalArgumentException("Node " + node + " is not a successor of " + nodes.get(i - 1) + " in the original graph.");
				}
				if (i < n - 1) {
					tentativeSuccessors.put(node, successorGenerator.generateSuccessors(node));
				}
			}

			/* replace successor map and current path variable */
			this.currentPathNodes.clear();
			this.currentPathNodes.addAll(nodes);
			this.currentPathEdges.clear();
			this.currentPathEdges.addAll(edges);
			this.successors.clear();
			this.successors.putAll(tentativeSuccessors);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void setCurrentPath(final int... decisions) {
		try {
			N root = this.currentPathNodes.isEmpty() ? ((SingleRootGenerator<N>) this.getGraphGenerator().getRootGenerator()).getRoot() : this.currentPathNodes.get(0);
			List<N> tentativePath = new ArrayList<>();
			tentativePath.add(root);
			Map<N, List<NodeExpansionDescription<N, A>>> tentativeSuccessors = new HashMap<>();
			SuccessorGenerator<N, A> successorGenerator = this.getGraphGenerator().getSuccessorGenerator();
			int n = decisions.length;
			for (int i = 0; i < n; i++) {
				N node = tentativePath.get(i);
				tentativeSuccessors.put(node, successorGenerator.generateSuccessors(node));
				tentativePath.add(tentativeSuccessors.get(node).get(decisions[i]).getTo());
			}

			/* replace successor map and current path variable */
			this.currentPathNodes.clear();
			this.currentPathNodes.addAll(tentativePath);
			this.successors.clear();
			this.successors.putAll(tentativeSuccessors);
			this.checkPathConsistency(this.currentPathNodes);
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
				if (this.successors.get(last).stream().noneMatch(nd -> nd.getTo().equals(node))) {
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