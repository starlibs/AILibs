package jaicore.search.algorithms.andor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.basic.sets.LDSRelationComputer;
import jaicore.basic.sets.RelationComputationProblem;
import jaicore.graph.Graph;
import jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;

public class AndORBottomUpFilter<N, A, V extends Comparable<V>> extends AAlgorithm<GraphSearchInput<N, A>, Graph<N>> implements IGraphSearch<GraphSearchInput<N, A>, Graph<N>, N, A> {

	private Logger logger = LoggerFactory.getLogger(AndORBottomUpFilter.class);
	private String loggerName;

	public class InnerNodeLabel {
		InnerNodeLabel parent;
		int val;
		N node;
		NodeType type;
		boolean evaluated;

		public InnerNodeLabel(final N node, final NodeType type) {
			super();
			this.node = node;
			this.type = type;
		}

		public String path() {
			if (this.parent == null) {
				return this.val + "";
			}
			return this.parent.path() + " -> " + this.val;
		}
	}

	class EvaluatedGraph {
		Graph<N> graph;
		V value;
	}

	private final Graph<InnerNodeLabel> graph = new Graph<>();
	private final IObjectEvaluator<Graph<N>, V> evaluator; // to evaluate (sub-)solutions
	private Graph<N> bestSolutionBase;
	private final int nodeLimit;

	public AndORBottomUpFilter(final GraphGenerator<N, A> gg, final IObjectEvaluator<Graph<N>, V> pEvaluator) {
		this(gg, pEvaluator, 1);
	}

	public AndORBottomUpFilter(final GraphGenerator<N, A> gg, final IObjectEvaluator<Graph<N>, V> pEvaluator, final int andNodeLimit) {
		super(new GraphSearchInput<>(gg));
		this.evaluator = pEvaluator;
		this.nodeLimit = andNodeLimit;
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmException, AlgorithmExecutionCanceledException {
		switch (this.getState()) {
		case created:

			/* step 1: construct the whole graph */
			Queue<InnerNodeLabel> open = new LinkedList<>();
			InnerNodeLabel root = new InnerNodeLabel(((SingleRootGenerator<N>) this.getInput().getGraphGenerator().getRootGenerator()).getRoot(), NodeType.AND);
			root.val = 0;
			open.add(root);
			this.post(new GraphInitializedEvent<N>(this.getId(), root.node));
			this.graph.addItem(root);
			while (!open.isEmpty()) {
				InnerNodeLabel n = open.poll();
				try {
					int generatedChildren = 0;
					for (NodeExpansionDescription<N, A> descr : this.getInput().getGraphGenerator().getSuccessorGenerator().generateSuccessors(n.node)) {
						InnerNodeLabel newNode = new InnerNodeLabel(descr.getTo(), descr.getTypeOfToNode());
						newNode.parent = n;
						newNode.val = generatedChildren;
						synchronized (this.graph) {
							this.graph.addItem(newNode);
							this.logger.trace("Added {}-node {} as a child to {}", newNode.type, newNode, n);
							this.graph.addEdge(n, newNode);
						}
						open.add(newNode);
						generatedChildren++;
						this.post(new NodeAddedEvent<N>(this.getId(), n.node, newNode.node, descr.getTypeOfToNode() == NodeType.OR ? "or" : "and"));
					}
					this.logger.debug("Node expansion of {}-node {} completed. Generated {} successors.", n.type, n, generatedChildren);
				} catch (Exception e) {
					throw new AlgorithmException(e, "Received exception in algorithm initialization.");
				}
			}
			this.logger.info("Size: {}", this.graph.getItems().size());
			return this.activate();

		case active:
			this.logger.debug("timeout: {}", this.getTimeout());

			/* now compute best local values bottom up */
			Queue<EvaluatedGraph> bestSolutions;
			try {
				bestSolutions = this.filterNodeSolution(this.graph.getRoot());
				this.logger.info("Number of solutions: {}", bestSolutions.size());
				if (!bestSolutions.isEmpty()) {
					this.bestSolutionBase = bestSolutions.poll().graph;
				}
				return this.terminate();
			} catch (ObjectEvaluationFailedException e) {
				throw new AlgorithmException(e, "Could not evaluate solution.");
			}

		default:
			throw new IllegalStateException("No handler defined for state " + this.getState());
		}

	}

	/**
	 * assumes that solution paths for children have been computed already
	 *
	 * @throws AlgorithmException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws AlgorithmExecutionCanceledException
	 *
	 * @throws Exception
	 */
	private Queue<EvaluatedGraph> filterNodeSolution(final InnerNodeLabel node) throws AlgorithmTimeoutedException, InterruptedException, ObjectEvaluationFailedException, AlgorithmExecutionCanceledException {

		Queue<EvaluatedGraph> filteredSolutions = new PriorityQueue<>((p1, p2) -> p2.value.compareTo(p1.value)); // a list of paths ordered by their (believed) importance in ASCENDING ORDER
		// (unimportant first, because these are drained)
		assert !node.evaluated : "Node " + node + " is filtered for the 2nd time already!";
		node.evaluated = true;
		this.logger.debug("Computing solutions for ({})-Node {} with {} children.", node.type, node.node, this.graph.getSuccessors(node).size());

		/* if this is a leaf node, just return itself */
		if (this.graph.getSuccessors(node).isEmpty()) {
			EvaluatedGraph evaluatedGraph = new EvaluatedGraph();
			Graph<N> subGraph = new Graph<>();
			subGraph.addItem(node.node);
			evaluatedGraph.graph = subGraph;
			evaluatedGraph.value = this.evaluator.evaluate(subGraph);
			filteredSolutions.add(evaluatedGraph);
			this.logger.debug("Returning one single-node solution graph for leaf node {}", node.node);
			return filteredSolutions;
		}

		/* otherwise first compute the values for all children */
		Map<InnerNodeLabel, Queue<EvaluatedGraph>> subSolutions = new HashMap<>();
		this.logger.debug("Computing subsolutions of node {}", node);
		for (InnerNodeLabel child : this.graph.getSuccessors(node)) {
			Queue<EvaluatedGraph> filteredSolutionsUnderChild = this.filterNodeSolution(child);
			if (filteredSolutionsUnderChild.isEmpty()) {
				this.logger.info("Canceling further examinations as we have a node without sub-solutions.");
				return new LinkedList<>();
			}
			subSolutions.put(child, filteredSolutionsUnderChild);
			if (this.logger.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder();
				subSolutions.get(child).forEach(l -> sb.append("\n\tGraph Evaluation: " + l.value + "\n\tGraph representation: \n" + l.graph.getLineBasedStringRepresentation(2).replaceAll("\n", "\n\t\t")));
				this.logger.debug("Child {} has {} solutions: {}", child, filteredSolutionsUnderChild.size(), sb);
			}
		}

		/*
		 * if this is an AND node, combine all solution paths of the children and choose
		 * the best COMBINATIONs
		 */
		if (node.type == NodeType.AND) {

			/* compute cartesian product of best subsolutions of children */
			int k = (int) Math.ceil(Math.pow(this.nodeLimit, 1f / subSolutions.size())); // the number of subsolution to consider for each child
			List<Collection<EvaluatedGraph>> subSolutionsPerChild = new ArrayList<>();
			for (Queue<EvaluatedGraph> child : subSolutions.values()) {
				List<EvaluatedGraph> bestSubSolutionsOfThisChild = new ArrayList<>();
				if (this.logger.isDebugEnabled()) { // complex log argument, so explicitly condition its execution
					this.logger.debug("Adding {}/{} subsolutions of child into the cartesian product input.", Math.min(k, child.size()), child.size());
				}
				for (int i = 0; i < k; i++) {
					EvaluatedGraph subsolution = child.poll();
					if (subsolution != null) {
						bestSubSolutionsOfThisChild.add(subsolution);
					}
				}
				subSolutionsPerChild.add(bestSubSolutionsOfThisChild);
			}
			/* for each such combination, build the grpah and store it */
			int i = 0;
			LDSRelationComputer<EvaluatedGraph> cartesianProductBuilder = new LDSRelationComputer<>(new RelationComputationProblem<>(subSolutionsPerChild, subSolutionCombination -> {
				EvaluatedGraph extendedSolutionBase = new EvaluatedGraph();
				extendedSolutionBase.graph = new Graph<>();
				extendedSolutionBase.graph.addItem(node.node);
				for (EvaluatedGraph subSolution : subSolutionCombination) {
					assert subSolution != null;
					extendedSolutionBase.graph.addGraph(subSolution.graph);
					extendedSolutionBase.graph.addEdge(node.node, subSolution.graph.getRoot());
				}
				try {
					extendedSolutionBase.value = this.evaluator.evaluate(extendedSolutionBase.graph);
				} catch (TimeoutException | ObjectEvaluationFailedException e) {
					this.logger.warn("Received exception {}.", e);
				}
				catch (InterruptedException e) {
					assert !Thread.currentThread().isInterrupted() : "The interrupted-flag should not be true when an InterruptedException is thrown!";
					this.logger.info("Thread was interrupted, cannot process this exception here, so reinterrupting the thread.");
					Thread.currentThread().interrupt(); // no controlled interrupt necessary, because this is only a re-interruption
				}
				boolean isDouble = extendedSolutionBase.value instanceof Double;
				boolean validSolution = isDouble ? !(extendedSolutionBase.value.equals(Double.NaN)) : extendedSolutionBase.value != null;
				if (!validSolution && this.logger.isDebugEnabled()) {
					this.logger.debug("\tCutting of sub-solution combination at level {}/{} as cost function returned {}. The following combined solution graph was subject of evaluation (one path per line, ommitted nodes are equal to the above line(s)):\n{}", subSolutionCombination.size(), subSolutionsPerChild.size(), extendedSolutionBase.value, extendedSolutionBase.graph.getLineBasedStringRepresentation(2));
				}
				return validSolution;
			}));
			List<EvaluatedGraph> subSolutionCombination;
			while ((subSolutionCombination = cartesianProductBuilder.nextTuple()) != null && i++ < 2 * this.nodeLimit) {
				EvaluatedGraph extendedSolutionBase = new EvaluatedGraph();
				extendedSolutionBase.graph = new Graph<>();
				extendedSolutionBase.graph.addItem(node.node);
				for (EvaluatedGraph subSolution : subSolutionCombination) {
					assert subSolution != null;
					extendedSolutionBase.graph.addGraph(subSolution.graph);
					extendedSolutionBase.graph.addEdge(node.node, subSolution.graph.getRoot());
				}
				extendedSolutionBase.value = this.evaluator.evaluate(extendedSolutionBase.graph);
				if (this.logger.isTraceEnabled()) { // costly arguments, only compute if trace is enabled
					this.logger.trace("Combination {} of subsolutions with performances {} yields an aggregate performance value of {}", i, subSolutionCombination.stream().map(g -> ""+g.value).collect(Collectors.joining(", ")),
							extendedSolutionBase.value);
				}
				filteredSolutions.add(extendedSolutionBase);
			}
			this.logger.trace("Determined {} sub-solutions for AND-node with {} children.", filteredSolutions.size(), subSolutions.size());
		}

		/* if this is an OR node, choose the best solution paths of the children */
		else {
			this.logger.debug("OR-Node: Selecting best child solution out of {}", subSolutions.size());
			for (Entry<InnerNodeLabel, Queue<EvaluatedGraph>> child : subSolutions.entrySet()) {
				this.logger.trace("Child {} has {} sub-solutions.", child.getKey(), child.getValue().size());
				for (EvaluatedGraph solutionBase : child.getValue()) {
					EvaluatedGraph extendedSolutionBase = new EvaluatedGraph();
					extendedSolutionBase.graph = new Graph<>(solutionBase.graph);
					extendedSolutionBase.graph.addItem(node.node);
					extendedSolutionBase.graph.addEdge(node.node, child.getKey().node);
					extendedSolutionBase.value = solutionBase.value;
					filteredSolutions.add(extendedSolutionBase);
				}
			}
		}

		/* order solutions by value */
		filteredSolutions = new LinkedList<>(filteredSolutions.stream().sorted((g1, g2) -> g1.value.compareTo(g2.value)).limit(this.nodeLimit).collect(Collectors.toList()));
		if (this.logger.isDebugEnabled()) { // path computation is not cheap, so condition this explicitly
			this.logger.debug("{} accepted sub-solutions of {}-node {} with {} children.", filteredSolutions.size(), node.type, node.path(), subSolutions.size());
		}
		int i = 1;
		for (EvaluatedGraph g : filteredSolutions) {
			this.logger.debug("\tValue of sub-solution #{}: {}", i, g.value);
			i++;
		}
		return filteredSolutions;
	}

	@Override
	public Graph<N> call() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmException, AlgorithmExecutionCanceledException {
		while (this.hasNext()) {
			this.nextWithException();
		}
		return this.bestSolutionBase;
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
		if (this.evaluator instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.evaluator).setLoggerName(name + ".eval");
		}
		super.setLoggerName(this.loggerName + "._orgraphsearch");
	}

	@Override
	public GraphGenerator<N, A> getGraphGenerator() {
		return this.getInput().getGraphGenerator();
	}
}