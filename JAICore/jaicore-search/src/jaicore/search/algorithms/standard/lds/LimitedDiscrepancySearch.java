package jaicore.search.algorithms.standard.lds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AlgorithmCanceledEvent;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInterruptedEvent;
import jaicore.basic.algorithm.SolutionCandidateFoundEvent;
import jaicore.graph.TreeNode;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.search.algorithms.standard.AbstractORGraphSearch;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.NodeRecommendedTree;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
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
public class LimitedDiscrepancySearch<T, A, V extends Comparable<V>> extends AbstractORGraphSearch<NodeRecommendedTree<T, A>, EvaluatedSearchGraphPath<T, A, V>, T, A, V, TreeNode<T>, A> {

	/* logging */
	private final Logger logger = LoggerFactory.getLogger(LimitedDiscrepancySearch.class);

	/* communication */
	protected TreeNode<T> traversalTree;
	protected Collection<TreeNode<T>> expanded = new HashSet<>();
	protected final Queue<EvaluatedSearchGraphPath<T, A, V>> solutions = new LinkedBlockingQueue<>();

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

	public LimitedDiscrepancySearch(NodeRecommendedTree<T, A> problemInput) {
		super(problemInput);
		this.rootGenerator = (SingleRootGenerator<T>) problem.getGraphGenerator().getRootGenerator();
		this.successorGenerator = problem.getGraphGenerator().getSuccessorGenerator();
		checkGoalPropertyOnEntirePath = !(problem.getGraphGenerator().getGoalTester() instanceof NodeGoalTester);
		if (checkGoalPropertyOnEntirePath) {
			this.nodeGoalTester = null;
			this.pathGoalTester = (PathGoalTester<T>) problem.getGraphGenerator().getGoalTester();
			;
		} else {
			this.nodeGoalTester = (NodeGoalTester<T>) problem.getGraphGenerator().getGoalTester();
			this.pathGoalTester = null;
		}
		this.heuristic = problemInput.getRecommender();
	}

	@Override
	public AlgorithmEvent nextWithException() {
		switch (getState()) {
		case created: {
			this.traversalTree = newNode(null, rootGenerator.getRoot());
			postEvent(new GraphInitializedEvent<>(traversalTree));
			return activate();
		}
		case active: {
			try {
				AlgorithmEvent event;
				event = ldsProbe(traversalTree);
				if (event instanceof NoMoreNodesOnLevelEvent) {
					if (!probeHasExpandedNode) {
						logger.info("Probe process has not expanded any node, finishing alogrithm");
						shutdown();;
						return new AlgorithmFinishedEvent();
					} else {
						logger.info("Probe process has not more nodes to be considered, restarting with augmented k {}", maxK + 1);
						maxK++;
						currentK = maxK;
						probeHasExpandedNode = false;
						return event;
					}
				} else {
					logger.info("Returning event {}", event);
					return event;
				}
			} catch (InterruptedException e) {
				return new AlgorithmInterruptedEvent();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		default:
			throw new IllegalStateException("The algorithm cannot do anything in state " + getState());
		}
	}

	/**
	 * Computes a solution path that deviates k times from the heuristic (if possible)
	 * 
	 * @param node
	 * @param k
	 * @return
	 */
	private AlgorithmEvent ldsProbe(TreeNode<T> node) throws Exception {
		logger.info("Probing under node {} with k = {}", node.getValue(), currentK);

		/* return solution event if this is a solution node */
		if (nodeGoalTester.isGoal(node.getValue())) {
			List<T> path = node.getValuesOnPathFromRoot();
			EvaluatedSearchGraphPath<T, A, V> solution = new EvaluatedSearchGraphPath<>(path, null, null);
			solutions.add(solution);
			return new SolutionCandidateFoundEvent<>(solution);
		}

		/* if this node has not been expanded, compute successors and the priorities among them and attach them to search graph */
		if (!expanded.contains(node)) {
			expanded.add(node);
			probeHasExpandedNode = true;
			Collection<NodeExpansionDescription<T, A>> succ = successorGenerator.generateSuccessors(node.getValue());
			if (succ == null || succ.isEmpty())
				return new NoMoreNodesOnLevelEvent();
			List<NodeExpansionDescription<T, A>> prioSucc = succ.stream().sorted((d1, d2) -> heuristic.compare(d1.getTo(), d2.getTo())).collect(Collectors.toList());
			List<TreeNode<T>> generatedNodes = new ArrayList<>();
			for (NodeExpansionDescription<T, A> successorDescription : prioSucc) {
				if (Thread.currentThread().isInterrupted()) {
					this.cancel();
					throw new InterruptedException("Thread that executes LDS has been interrupted. The LDS has been canceled.");
				}
				if (this.isCanceled())
					return new AlgorithmCanceledEvent();
				TreeNode<T> newNode = newNode(node, successorDescription.getTo());
				generatedNodes.add(newNode);
			}
		} else
			logger.info("Not expanding node {} again.", node.getValue());
		List<TreeNode<T>> children = node.getChildren();
		if (children.isEmpty())
			return new NoMoreNodesOnLevelEvent();

		/* otherwise, deviate from the heuristic if this brings a solution */
		/* if no more discrepancies are allowed, keep searching under the first child */
		if (currentK > 0 && children.size() > 1) {
			currentK--;
			return ldsProbe(children.get(1));
		}
		return ldsProbe(children.get(0));
	}

	protected synchronized TreeNode<T> newNode(TreeNode<T> parent, T newNode) {

		/* attach new node to traversal tree */
		TreeNode<T> newTree = parent != null ? parent.addChild(newNode) : new TreeNode<>(newNode);

		/* send events for this new node */
		if (parent != null) {
			boolean isGoal = nodeGoalTester.isGoal(newNode);
			postEvent(new NodeReachedEvent<TreeNode<T>>(parent, newTree, "or_" + (isGoal ? "solution" : "created")));
		}
		return newTree;
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		logger.warn("Currently no support for parallelization");
	}


	@Override
	public EvaluatedSearchGraphPath<T, A, V> call() throws Exception {
		nextSolution();
		return solutions.peek();
	}

	@Override
	public int getNumCPUs() {
		return 1;
	}

	@Override
	public GraphGenerator<T, A> getGraphGenerator() {
		return getInput().getGraphGenerator();
	}

	@Override
	public EvaluatedSearchGraphPath<T, A, V> getBestSeenSolution() {
		throw new UnsupportedOperationException();
	}

	@Override
	public EvaluatedSearchGraphPath<T, A, V> getSolutionProvidedToCall() {
		return solutions.peek();
	}
}
