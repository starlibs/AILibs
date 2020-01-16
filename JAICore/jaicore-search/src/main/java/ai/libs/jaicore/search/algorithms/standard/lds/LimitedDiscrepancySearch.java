package ai.libs.jaicore.search.algorithms.standard.lds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.algorithm.ASolutionCandidateFoundEvent;
import ai.libs.jaicore.graph.TreeNode;
import ai.libs.jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import ai.libs.jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNodeRecommenderInput;

/**
 * Implementation of the algorithm presented in
 *
 * @inproceedings{harvey1995, title={Limited discrepancy search}, author={Harvey, William D and Ginsberg, Matthew L}, booktitle={IJCAI (1)}, pages={607--615}, year={1995} }
 *
 * @author fmohr
 *
 */
public class LimitedDiscrepancySearch<I extends GraphSearchWithNodeRecommenderInput<N, A>, N, A, V extends Comparable<V>> extends AOptimalPathInORGraphSearch<I, N, A, V> {

	/* logging */
	private Logger logger = LoggerFactory.getLogger(LimitedDiscrepancySearch.class);
	private String loggerName;

	/* communication */
	protected TreeNode<N> traversalTree;
	protected Map<N, A> actionToNode = new HashMap<>();
	protected Collection<TreeNode<N>> expanded = new HashSet<>();
	protected Collection<TreeNode<N>> exhausted = new HashSet<>();

	/* graph construction helpers */
	protected final ISingleRootGenerator<N> rootGenerator;
	protected final ISuccessorGenerator<N, A> successorGenerator;
	protected final IPathGoalTester<N, A> pathGoalTester;

	/* graph traversal helpers */
	protected final Comparator<N> heuristic;

	/* algorithm state */
	private int maxK = 0;
	private int currentK = 0;

	public LimitedDiscrepancySearch(final I problemInput) {
		super(problemInput);
		this.rootGenerator = (ISingleRootGenerator<N>) this.getInput().getGraphGenerator().getRootGenerator();
		this.successorGenerator = this.getInput().getGraphGenerator().getSuccessorGenerator();
		this.pathGoalTester = this.getInput().getGoalTester();
		this.heuristic = problemInput.getRecommender();
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException {
		this.registerActiveThread();
		try {
			switch (this.getState()) {
			case CREATED:
				this.traversalTree = this.newNode(null, this.rootGenerator.getRoot());
				this.post(new GraphInitializedEvent<>(this, this.traversalTree));
				return this.activate();

			case ACTIVE:
				this.currentK = this.maxK;
				IAlgorithmEvent event = this.ldsProbe(this.traversalTree);
				if (event instanceof NoMoreNodesOnLevelEvent) {
					if (this.currentK == 0) { // if all deviations have been used, increase number of maximum deviations
						this.logger.info("Probe process has no more nodes to be considered, restarting with augmented k {}", this.maxK + 1);
						this.maxK++;
						return event;
					}
					else {
						return this.terminate(); // otherwise, terminate (allowing for more deviations will not yield more results)
					}
				} else {
					this.logger.info("Returning event {}", event);
					this.post(event);
					return event;
				}
			default:
				throw new IllegalStateException("The algorithm cannot do anything in state " + this.getState());
			}
		}
		finally {
			this.unregisterActiveThread();
		}
	}

	private void updateExhaustMap(final TreeNode<N> node) {
		if (node == null) {
			return;
		}
		if (this.exhausted.contains(node)) {
			this.updateExhaustMap(node.getParent());
		}
		if (this.exhausted.containsAll(node.getChildren())) {
			this.exhausted.add(node);
			this.updateExhaustMap(node.getParent());
		}
	}

	/**
	 * Computes a solution path that deviates k times from the heuristic (if possible)
	 *
	 * @param node
	 * @param k
	 * @return
	 * @throws InterruptedException
	 * @throws AlgorithmExecutionCanceledException
	 * @throws AlgorithmTimeoutedException
	 * @throws AlgorithmException
	 */
	private IAlgorithmEvent ldsProbe(final TreeNode<N> node) throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException {
		this.logger.debug("Probing under node {} with k = {}. Exhausted: {}", node.getValue(), this.currentK, this.exhausted.contains(node));

		/* return solution event if this is a solution node */
		if (this.pathGoalTester.isGoal(this.getPathForGoalCheck(node.getValue()))) {
			this.updateExhaustMap(node);
			List<N> path = node.getValuesOnPathFromRoot();
			List<A> actions = path.stream().map(n -> this.actionToNode.get(n)).filter(Objects::nonNull).collect(Collectors.toList());
			EvaluatedSearchGraphPath<N, A, V> solution = new EvaluatedSearchGraphPath<>(path, actions, null);
			this.updateBestSeenSolution(solution);
			this.logger.debug("Found solution {}.", node.getValue());
			return new ASolutionCandidateFoundEvent<>(this, solution);
		}

		/* if this node has not been expanded, compute successors and the priorities among them and attach them to search graph */
		if (!this.expanded.contains(node)) {
			this.expanded.add(node);
			this.logger.debug("Starting successor generation of {}", node.getValue());
			long start = System.currentTimeMillis();
			Collection<INewNodeDescription<N, A>> succ = this.computeTimeoutAware(() -> this.successorGenerator.generateSuccessors(node.getValue()), "Successor generation" , true);
			if (succ == null || succ.isEmpty()) {
				this.logger.debug("No successors were generated.");
				return new NoMoreNodesOnLevelEvent(this);
			}
			this.logger.debug("Computed {} successors in {}ms. Attaching the nodes to the local model.", succ.size(), System.currentTimeMillis() - start);
			List<INewNodeDescription<N, A>> prioSucc = succ.stream().sorted((d1, d2) -> this.heuristic.compare(d1.getTo(), d2.getTo())).collect(Collectors.toList());
			this.checkAndConductTermination();
			List<TreeNode<N>> generatedNodes = new ArrayList<>();
			long lastCheck = System.currentTimeMillis();
			for (INewNodeDescription<N, A> successorDescription : prioSucc) {
				if (System.currentTimeMillis() - lastCheck > 10) {
					this.checkAndConductTermination();
					lastCheck = System.currentTimeMillis();
				}
				TreeNode<N> newNode = this.newNode(node, successorDescription.getTo());
				this.actionToNode.put(successorDescription.getTo(), successorDescription.getArcLabel());
				generatedNodes.add(newNode);
			}
			this.logger.debug("Local model updated.");
			this.checkAndConductTermination();
		} else {
			this.logger.info("Not expanding node {} again.", node.getValue());
		}
		List<TreeNode<N>> children = node.getChildren();
		if (children.isEmpty()) {
			return new NoMoreNodesOnLevelEvent(this);
		}

		/* if no deviation is allowed, return the probe for the first child (unless that child is already exhausted) */
		if (this.currentK == 0 || children.size() == 1) {
			boolean onlyAdmissibleChildExhausted = this.exhausted.contains(children.get(0));
			this.logger.debug("No deviation allowed or only one child node. Probing this child (if not, the reason is that it is exhausted already): {}", !onlyAdmissibleChildExhausted);
			return !onlyAdmissibleChildExhausted ? this.ldsProbe(children.get(0)) : new NoMoreNodesOnLevelEvent(this);
		}

		/* deviate from the heuristic. If no more discrepancies are allowed, keep searching under the first child unless that child has been exhausted */
		this.currentK--;
		this.logger.debug("Deviating from heuristic. Decreased current k to {}", this.currentK);
		if (this.exhausted.contains(children.get(1))) {
			return new NoMoreNodesOnLevelEvent(this);
		}
		return this.ldsProbe(children.get(1));
	}

	protected synchronized TreeNode<N> newNode(final TreeNode<N> parent, final N newNode) {

		/* attach new node to traversal tree */
		TreeNode<N> newTree = parent != null ? parent.addChild(newNode) : new TreeNode<>(newNode);

		/* send events for this new node */
		if (parent != null) {
			boolean isGoal = this.pathGoalTester.isGoal(this.getPathForGoalCheck(newNode));
			this.post(new NodeAddedEvent<TreeNode<N>>(this, parent, newTree, "or_" + (isGoal ? "solution" : "created")));
		}
		return newTree;
	}

	private ILabeledPath<N, A> getPathForGoalCheck(final N node) {
		return new BackPointerPath<>(null, node, null);
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		super.setLoggerName(this.loggerName + "._orgraphsearch");
	}
}
