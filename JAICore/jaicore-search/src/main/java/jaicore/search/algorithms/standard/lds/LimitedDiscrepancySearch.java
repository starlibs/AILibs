package jaicore.search.algorithms.standard.lds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.ASolutionCandidateFoundEvent;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.graph.TreeNode;
import jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.probleminputs.GraphSearchWithNodeRecommenderInput;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.PathGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

/**
 * Implementation of the algorithm presented in
 *
 * @inproceedings{harvey1995, title={Limited discrepancy search}, author={Harvey, William D and Ginsberg, Matthew L}, booktitle={IJCAI (1)}, pages={607--615}, year={1995} }
 *
 * @author fmohr
 *
 */
public class LimitedDiscrepancySearch<T, A, V extends Comparable<V>> extends AOptimalPathInORGraphSearch<GraphSearchWithNodeRecommenderInput<T, A>, T, A, V> {

	/* logging */
	private Logger logger = LoggerFactory.getLogger(LimitedDiscrepancySearch.class);
	private String loggerName;

	/* communication */
	protected TreeNode<T> traversalTree;
	protected Collection<TreeNode<T>> expanded = new HashSet<>();
	protected Collection<TreeNode<T>> exhausted = new HashSet<>();

	/* graph construction helpers */
	protected final SingleRootGenerator<T> rootGenerator;
	protected final SuccessorGenerator<T, A> successorGenerator;
	protected final boolean checkGoalPropertyOnEntirePath;
	protected final PathGoalTester<T> pathGoalTester;
	protected final NodeGoalTester<T> nodeGoalTester;

	/* graph traversal helpers */
	protected final Comparator<T> heuristic;

	/* algorithm state */
	private int maxK = 0;
	private int currentK = 0;

	public LimitedDiscrepancySearch(final GraphSearchWithNodeRecommenderInput<T, A> problemInput) {
		super(problemInput);
		this.rootGenerator = (SingleRootGenerator<T>) this.getInput().getGraphGenerator().getRootGenerator();
		this.successorGenerator = this.getInput().getGraphGenerator().getSuccessorGenerator();
		this.checkGoalPropertyOnEntirePath = !(this.getInput().getGraphGenerator().getGoalTester() instanceof NodeGoalTester);
		if (this.checkGoalPropertyOnEntirePath) {
			this.nodeGoalTester = null;
			this.pathGoalTester = (PathGoalTester<T>) this.getInput().getGraphGenerator().getGoalTester();
		} else {
			this.nodeGoalTester = (NodeGoalTester<T>) this.getInput().getGraphGenerator().getGoalTester();
			this.pathGoalTester = null;
		}
		this.heuristic = problemInput.getRecommender();
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException {
		this.registerActiveThread();
		try {
			switch (this.getState()) {
			case CREATED:
				this.traversalTree = this.newNode(null, this.rootGenerator.getRoot());
				this.post(new GraphInitializedEvent<>(this.getId(), this.traversalTree));
				return this.activate();

			case ACTIVE:
				this.currentK = this.maxK;
				AlgorithmEvent event = this.ldsProbe(this.traversalTree);
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

	private void updateExhaustMap(final TreeNode<T> node) {
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
	private AlgorithmEvent ldsProbe(final TreeNode<T> node) throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException {
		this.logger.debug("Probing under node {} with k = {}. Exhausted: {}", node.getValue(), this.currentK, this.exhausted.contains(node));

		/* return solution event if this is a solution node */
		if (this.nodeGoalTester.isGoal(node.getValue())) {
			this.updateExhaustMap(node);
			List<T> path = node.getValuesOnPathFromRoot();
			EvaluatedSearchGraphPath<T, A, V> solution = new EvaluatedSearchGraphPath<>(path, null, null);
			this.updateBestSeenSolution(solution);
			this.logger.debug("Found solution {}.", node.getValue());
			return new ASolutionCandidateFoundEvent<>(this.getId(), solution);
		}

		/* if this node has not been expanded, compute successors and the priorities among them and attach them to search graph */
		if (!this.expanded.contains(node)) {
			this.expanded.add(node);
			this.logger.debug("Starting successor generation of {}", node.getValue());
			long start = System.currentTimeMillis();
			Collection<NodeExpansionDescription<T, A>> succ = this.computeTimeoutAware(() -> this.successorGenerator.generateSuccessors(node.getValue()), "Successor generation" , true);
			if (succ == null || succ.isEmpty()) {
				this.logger.debug("No successors were generated.");
				return new NoMoreNodesOnLevelEvent(this.getId());
			}
			this.logger.debug("Computed {} successors in {}ms. Attaching the nodes to the local model.", succ.size(), System.currentTimeMillis() - start);
			List<NodeExpansionDescription<T, A>> prioSucc = succ.stream().sorted((d1, d2) -> this.heuristic.compare(d1.getTo(), d2.getTo())).collect(Collectors.toList());
			this.checkAndConductTermination();
			List<TreeNode<T>> generatedNodes = new ArrayList<>();
			long lastCheck = System.currentTimeMillis();
			for (NodeExpansionDescription<T, A> successorDescription : prioSucc) {
				if (System.currentTimeMillis() - lastCheck > 10) {
					this.checkAndConductTermination();
					lastCheck = System.currentTimeMillis();
				}
				TreeNode<T> newNode = this.newNode(node, successorDescription.getTo());
				generatedNodes.add(newNode);
			}
			this.logger.debug("Local model updated.");
			this.checkAndConductTermination();
		} else {
			this.logger.info("Not expanding node {} again.", node.getValue());
		}
		List<TreeNode<T>> children = node.getChildren();
		if (children.isEmpty()) {
			return new NoMoreNodesOnLevelEvent(this.getId());
		}

		/* if no deviation is allowed, return the probe for the first child (unless that child is already exhausted) */
		if (this.currentK == 0 || children.size() == 1) {
			boolean onlyAdmissibleChildExhausted = this.exhausted.contains(children.get(0));
			this.logger.debug("No deviation allowed or only one child node. Probing this child (if not, the reason is that it is exhausted already): {}", !onlyAdmissibleChildExhausted);
			return !onlyAdmissibleChildExhausted ? this.ldsProbe(children.get(0)) : new NoMoreNodesOnLevelEvent(this.getId());
		}

		/* deviate from the heuristic. If no more discrepancies are allowed, keep searching under the first child unless that child has been exhausted */
		this.currentK--;
		this.logger.debug("Deviating from heuristic. Decreased current k to {}", this.currentK);
		if (this.exhausted.contains(children.get(1))) {
			return new NoMoreNodesOnLevelEvent(this.getId());
		}
		return this.ldsProbe(children.get(1));
	}

	protected synchronized TreeNode<T> newNode(final TreeNode<T> parent, final T newNode) {

		/* attach new node to traversal tree */
		TreeNode<T> newTree = parent != null ? parent.addChild(newNode) : new TreeNode<>(newNode);

		/* send events for this new node */
		if (parent != null) {
			boolean isGoal = this.nodeGoalTester.isGoal(newNode);
			this.post(new NodeAddedEvent<TreeNode<T>>(this.getId(), parent, newTree, "or_" + (isGoal ? "solution" : "created")));
		}
		return newTree;
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
