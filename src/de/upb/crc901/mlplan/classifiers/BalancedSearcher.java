package de.upb.crc901.mlplan.classifiers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.BestFirstEpsilon;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.OpenCollection;
import jaicore.search.structure.core.PriorityQueueOpen;

public class BalancedSearcher extends TwoPhaseHTNBasedPipelineSearcher<Double> {

	private static final Logger logger = LoggerFactory.getLogger(BalancedSearcher.class);

	private int timeoutPerNodeFComputation;
	private int explorationPhaseLength = 10;
	private int exploitationPhaseLength = 50;

	class RegionOversearchingPreventor implements INodeEvaluator<TFDNode, Integer> {

		private Map<String, Integer> counter = new ConcurrentHashMap<>();
		private Map<Node<TFDNode, ?>, Integer> cache = new HashMap<>();

		@Override
		public Integer f(Node<TFDNode, ?> node) throws Exception {
			String classifier = (String) node.getAnnotation("classifier");
			if (!cache.containsKey(node)) {

				/* increment counter for this classifier (if one is defined) */
				int score = 0;
				if (classifier != null) {
					if (!counter.containsKey(classifier)) {
						score = 1;
					} else {
						score = counter.get(classifier) + 1;
					}
					counter.put(classifier, score);
				}
				cache.put(node, score);
			}
			node.setAnnotation("ROP", cache.get(node));
			return cache.get(node);
		}
	}

	class ExploringNodeSelector<W extends Comparable<W>> implements OpenCollection<Node<TFDNode, Double>> {

		/* define secondary OPEN list, based on the exploration score */
		private final OpenCollection<Node<TFDNode, Double>> primaryOpen;
		private final PriorityQueueOpen<Node<TFDNode, Double>> secondaryOpen = new PriorityQueueOpen<>();
		private final INodeEvaluator<TFDNode, W> explorationEvaluator;
		private int selectedNodes = 0;
		private int exploredNodes = 0;
		private boolean exploring = false;

		public ExploringNodeSelector(OpenCollection<Node<TFDNode, Double>> pPrimaryOpen, INodeEvaluator<TFDNode, W> explorationEvaluator) {
			super();
			this.primaryOpen = pPrimaryOpen;
			this.explorationEvaluator = explorationEvaluator;
		}

		@Override
		public Node<TFDNode, Double> peek() {
			if (primaryOpen.peek().getInternalLabel() < 50)
				return primaryOpen.peek();

			if (!exploring) {
				selectedNodes++;
				if (selectedNodes % exploitationPhaseLength != 0) {
					// System.out.println("Exploiting ...");
					return primaryOpen.peek();
				}

				/* now chose particular node for expansion */
				Node<TFDNode, Double> nodeToBeExplored = primaryOpen.stream().min((n1, n2) -> {
					try {
						return explorationEvaluator.f(n1).compareTo(explorationEvaluator.f(n2));
					} catch (Exception e) {
						e.printStackTrace();
					}
					return 0;
				}).get();

				/* enable exploration with the node selected by the explorer evaluator */
				try {
					if (logger.isInfoEnabled())
						logger.info("Entering exploration phase under {} with exploration value: {}", nodeToBeExplored, explorationEvaluator.f(nodeToBeExplored));
				} catch (Exception e) {
					e.printStackTrace();
				}
				exploring = true;
				exploredNodes = 0;
				primaryOpen.remove(nodeToBeExplored);
				secondaryOpen.clear();
				secondaryOpen.add(nodeToBeExplored);
				return nodeToBeExplored;
			} else {
				exploredNodes++;
				if (exploredNodes > explorationPhaseLength || secondaryOpen.isEmpty()) {
					exploring = false;
					primaryOpen.addAll(secondaryOpen);
					secondaryOpen.clear();
					return primaryOpen.peek();
				}
				return secondaryOpen.peek();
			}
		}

		@Override
		public boolean add(Node<TFDNode, Double> node) {
			assert !contains(node) : "Node " + node + " is already there!";
			if (exploring) {
				return secondaryOpen.add(node);
			} else
				return primaryOpen.add(node);
		}

		@Override
		public boolean remove(Object node) {
			assert !(primaryOpen.contains(node) && secondaryOpen.contains(node)) : "A node (" + node + ") that is to be removed is in BOTH open lists!";
			if (exploring) {
				return secondaryOpen.remove(node) || primaryOpen.remove(node);
			} else {
				return primaryOpen.remove(node) || secondaryOpen.remove(node);
			}
		}

		@Override
		public boolean addAll(Collection<? extends Node<TFDNode, Double>> arg0) {
			if (exploring) {
				return secondaryOpen.addAll(arg0);
			} else
				return primaryOpen.addAll(arg0);
		}

		@Override
		public void clear() {
			primaryOpen.clear();
			secondaryOpen.clear();
		}

		@Override
		public boolean contains(Object arg0) {
			return primaryOpen.contains(arg0) || secondaryOpen.contains(arg0);
		}

		@Override
		public boolean containsAll(Collection<?> arg0) {
			for (Object o : arg0) {
				if (!contains(o))
					return false;
			}
			return true;
		}

		@Override
		public boolean isEmpty() {
			return primaryOpen.isEmpty() && secondaryOpen.isEmpty();
		}

		@Override
		public Iterator<Node<TFDNode, Double>> iterator() {
			return null;
		}

		@Override
		public boolean removeAll(Collection<?> arg0) {
			return primaryOpen.removeAll(arg0) && secondaryOpen.removeAll(arg0);
		}

		@Override
		public boolean retainAll(Collection<?> arg0) {
			return primaryOpen.retainAll(arg0) && secondaryOpen.retainAll(arg0);
		}

		@Override
		public int size() {
			return primaryOpen.size() + secondaryOpen.size();
		}

		@Override
		public Object[] toArray() {
			return primaryOpen.toArray();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] toArray(T[] arg0) {
			return (T[]) primaryOpen.toArray();
		}
	}

	public BalancedSearcher(Random random, int timeout) throws IOException {
		super(new File("testrsc/automl3.testset"), null, null);
		setRandom(random);
		setTimeout(timeout);
	}

	@Override
	protected ORGraphSearch<TFDNode, String, Double> createActualSearchObject(GraphGenerator<TFDNode, String> graphGenerator, INodeEvaluator<TFDNode, Double> evaluator,
			int numberOfCPUs) {

		BestFirstEpsilon<TFDNode, String, Integer> search = new BestFirstEpsilon<>(graphGenerator, evaluator, new RegionOversearchingPreventor(), 0.1, false);
		search.parallelizeNodeExpansion(numberOfCPUs);
		search.setTimeoutForComputationOfF(timeoutPerNodeFComputation, n -> null);
		search.setOpen(new ExploringNodeSelector<>(search.getOpen(), n -> {
			Object estimatedUpperBound2OptimalSolution = search.getNodeAnnotation(n.getPoint(), "EUBRD2OS");
			if (estimatedUpperBound2OptimalSolution == null) {
				logger.warn("Ignoring node {}, because EUBRD2OS-annotation is not available", n);
				return Double.MAX_VALUE;
			}
			return -1 * (double) estimatedUpperBound2OptimalSolution;
		}));
		return search;
	}

	public int getExplorationPhaseLength() {
		return explorationPhaseLength;
	}

	public void setExplorationPhaseLength(int explorationPhaseLength) {
		this.explorationPhaseLength = explorationPhaseLength;
	}

	public int getExploitationPhaseLength() {
		return exploitationPhaseLength;
	}

	public void setExploitationPhaseLength(int exploitationPhaseLength) {
		this.exploitationPhaseLength = exploitationPhaseLength;
	}

}
