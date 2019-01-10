package jaicore.search.algorithms.standard.lds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.events.AlgorithmCanceledEvent;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.events.AlgorithmInterruptedEvent;
import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import jaicore.graph.TreeNode;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
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
public class LimitedDiscrepancySearch<T, A, V extends Comparable<V>> extends AOptimalPathInORGraphSearch<GraphSearchWithNodeRecommenderInput<T, A>, T, A, V, TreeNode<T>, A> {

	/* logging */
	private Logger logger = LoggerFactory.getLogger(LimitedDiscrepancySearch.class);
	private String loggerName;

	/* communication */
	protected TreeNode<T> traversalTree;
	protected Collection<TreeNode<T>> expanded = new HashSet<>();

	/* graph construction helpers */
	protected final SingleRootGenerator<T> rootGenerator;
	protected final SuccessorGenerator<T, A> successorGenerator;
	protected final boolean checkGoalPropertyOnEntirePath;
	protected final PathGoalTester<T> pathGoalTester;
	protected final NodeGoalTester<T> nodeGoalTester;

	/* graph travesal helpers */
	protected final Comparator<T> heuristic;

	/* algorithm state */
	private int maxK = 1;
	private int currentK = 1;
	private boolean probeHasExpandedNode = false;

	public LimitedDiscrepancySearch(final GraphSearchWithNodeRecommenderInput<T, A> problemInput) {
		super(problemInput);
		this.rootGenerator = (SingleRootGenerator<T>) this.getInput().getGraphGenerator().getRootGenerator();
		this.successorGenerator = this.getInput().getGraphGenerator().getSuccessorGenerator();
		this.checkGoalPropertyOnEntirePath = !(this.getInput().getGraphGenerator().getGoalTester() instanceof NodeGoalTester);
		if (this.checkGoalPropertyOnEntirePath) {
			this.nodeGoalTester = null;
			this.pathGoalTester = (PathGoalTester<T>) this.getInput().getGraphGenerator().getGoalTester();
			;
		} else {
			this.nodeGoalTester = (NodeGoalTester<T>) this.getInput().getGraphGenerator().getGoalTester();
			this.pathGoalTester = null;
		}
		this.heuristic = problemInput.getRecommender();
	}

	@Override
	public AlgorithmEvent nextWithException() {
		switch (this.getState()) {
		case created: {
			this.traversalTree = this.newNode(null, this.rootGenerator.getRoot());
			this.post(new GraphInitializedEvent<>(this.traversalTree));
			return this.activate();
		}
		case active: {
			try {
				AlgorithmEvent event;
				event = this.ldsProbe(this.traversalTree);
				if (event instanceof NoMoreNodesOnLevelEvent) {
					if (!this.probeHasExpandedNode) {
						this.logger.info("Probe process has not expanded any node, finishing alogrithm");
						this.shutdown();
						;
						return new AlgorithmFinishedEvent();
					} else {
						this.logger.info("Probe process has not more nodes to be considered, restarting with augmented k {}", this.maxK + 1);
						this.maxK++;
						this.currentK = this.maxK;
						this.probeHasExpandedNode = false;
						return event;
					}
				} else {
					this.logger.info("Returning event {}", event);
					return event;
				}
			} catch (InterruptedException e) {
				return new AlgorithmInterruptedEvent();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		default:
			throw new IllegalStateException("The algorithm cannot do anything in state " + this.getState());
		}
	}

	/**
	 * Computes a solution path that deviates k times from the heuristic (if possible)
	 *
	 * @param node
	 * @param k
	 * @return
	 */
	private AlgorithmEvent ldsProbe(final TreeNode<T> node) throws Exception {
		this.logger.info("Probing under node {} with k = {}", node.getValue(), this.currentK);

		/* return solution event if this is a solution node */
		if (this.nodeGoalTester.isGoal(node.getValue())) {
			List<T> path = node.getValuesOnPathFromRoot();
			EvaluatedSearchGraphPath<T, A, V> solution = new EvaluatedSearchGraphPath<>(path, null, null);
			updateBestSeenSolution(solution);
			return new SolutionCandidateFoundEvent<>(solution);
		}

		/* if this node has not been expanded, compute successors and the priorities among them and attach them to search graph */
		if (!this.expanded.contains(node)) {
			this.expanded.add(node);
			this.probeHasExpandedNode = true;
			Collection<NodeExpansionDescription<T, A>> succ = this.successorGenerator.generateSuccessors(node.getValue());
			if (succ == null || succ.isEmpty()) {
				return new NoMoreNodesOnLevelEvent();
			}
			List<NodeExpansionDescription<T, A>> prioSucc = succ.stream().sorted((d1, d2) -> this.heuristic.compare(d1.getTo(), d2.getTo())).collect(Collectors.toList());
			List<TreeNode<T>> generatedNodes = new ArrayList<>();
			for (NodeExpansionDescription<T, A> successorDescription : prioSucc) {
				if (Thread.currentThread().isInterrupted()) {
					this.cancel();
					throw new InterruptedException("Thread that executes LDS has been interrupted. The LDS has been canceled.");
				}
				if (this.isCanceled()) {
					return new AlgorithmCanceledEvent();
				}
				TreeNode<T> newNode = this.newNode(node, successorDescription.getTo());
				generatedNodes.add(newNode);
			}
		} else {
			this.logger.info("Not expanding node {} again.", node.getValue());
		}
		List<TreeNode<T>> children = node.getChildren();
		if (children.isEmpty()) {
			return new NoMoreNodesOnLevelEvent();
		}

		/* otherwise, deviate from the heuristic if this brings a solution */
		/* if no more discrepancies are allowed, keep searching under the first child */
		if (this.currentK > 0 && children.size() > 1) {
			this.currentK--;
			return this.ldsProbe(children.get(1));
		}
		return this.ldsProbe(children.get(0));
	}

	protected synchronized TreeNode<T> newNode(final TreeNode<T> parent, final T newNode) {

		/* attach new node to traversal tree */
		TreeNode<T> newTree = parent != null ? parent.addChild(newNode) : new TreeNode<>(newNode);

		/* send events for this new node */
		if (parent != null) {
			boolean isGoal = this.nodeGoalTester.isGoal(newNode);
			this.post(new NodeReachedEvent<TreeNode<T>>(parent, newTree, "or_" + (isGoal ? "solution" : "created")));
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
