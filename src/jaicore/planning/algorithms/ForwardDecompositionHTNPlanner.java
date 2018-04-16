package jaicore.planning.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.planning.graphgenerators.task.ceociptfd.CEOCIPTFDGraphGenerator;
import jaicore.planning.graphgenerators.task.ceoctfd.CEOCTFDGraphGenerator;
import jaicore.planning.graphgenerators.task.tfd.TFDGraphGenerator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.planning.model.task.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.model.task.stn.STNPlanningProblem;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearchFactory;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearchFactory;
import jaicore.search.structure.core.GraphGenerator;

/**
 * Hierarchically create an object of type T
 * 
 * @author fmohr
 *
 * @param <T>
 */
public class ForwardDecompositionHTNPlanner<V extends Comparable<V>> implements IObservableGraphBasedHTNPlanningAlgorithm<TFDNode, String, V> {

	private final static Logger logger = LoggerFactory.getLogger(ForwardDecompositionHTNPlanner.class);

	/* this is a class to maintain fast access and management of solutions (no equals method, no comparisons of paths required) */
	private static class Solution {
		List<TFDNode> path;

		public Solution(List<TFDNode> path) {
			super();
			this.path = path;
		}
	}

	public class SolutionIterator implements Iterator<List<Action>> {

		private boolean initialized = false;
		private boolean moreSolutionsMightExist = true;
		private IObservableORGraphSearch<TFDNode, String, V> search;
		private Solution nextSolution;

		public SolutionIterator() {

			/* create search algorithm */
			GraphGenerator<TFDNode, String> graphGenerator = null;
			if (planningProblem instanceof CEOCIPSTNPlanningProblem) {
				graphGenerator = new CEOCIPTFDGraphGenerator((CEOCIPSTNPlanningProblem) planningProblem);
			}
			else if (planningProblem instanceof CEOCSTNPlanningProblem) {
				graphGenerator = new CEOCTFDGraphGenerator((CEOCSTNPlanningProblem) planningProblem);
			} 
			else if (planningProblem.getClass().equals(STNPlanningProblem.class)) {
				graphGenerator = new TFDGraphGenerator((STNPlanningProblem)planningProblem);
			}
			else {
				throw new IllegalArgumentException("HTN problems of class \"" + planningProblem.getClass().getName() + "\" are currently not supported.");
			}
			search = searchFactory.createSearch(graphGenerator, nodeEvaluator, numberOfCPUs);
		}

		@Override
		public boolean hasNext() {
			if (!initialized) {
				logger.info("Starting HTN planning process.");
				synchronized (listeners) {
					for (Object listener : listeners) {
						search.registerListener(listener);
					}
				}
				initialized = true;
			}
			if (!moreSolutionsMightExist)
				return false;
			List<TFDNode> solution = search.nextSolution();
			if (solution == null) {
				moreSolutionsMightExist = false;
				return false;
			}
			nextSolution = new Solution(solution);
			return true;
		}

		@Override
		public List<Action> next() {
			if (!moreSolutionsMightExist || (nextSolution == null && !hasNext()))
				throw new NoSuchElementException();
			List<Action> plan = nextSolution.path.stream().filter(n -> n.getAppliedAction() != null).map(n -> n.getAppliedAction()).collect(Collectors.toList());
			nextSolution = null;
			if (plan == null) {
				throw new IllegalStateException("Planner has no more solution even though hasNext said that there would be one.");
			}
			return plan;
		}

		public Map<String, Object> getSolutionAnnotations(List<TFDNode> solution) {
			return search.getAnnotationsOfReturnedSolution(solution);
		}

		public Object getSolutionAnnotation(List<TFDNode> solution, String annotation) {
			return search.getAnnotationOfReturnedSolution(solution, annotation);
		}

		public IObservableORGraphSearch<TFDNode, String, V> getSearch() {
			return search;
		}
	}

	/* core elements of the search */
	private final IHTNPlanningProblem planningProblem;
	private final IObservableORGraphSearchFactory<TFDNode, String, V> searchFactory;
	private final INodeEvaluator<TFDNode, V> nodeEvaluator;

	/* parameters relevant for functionality */
	private final int numberOfCPUs;
	private final Collection<Object> listeners = new ArrayList<>();
	public ForwardDecompositionHTNPlanner(IHTNPlanningProblem problem, int numberOfCPUs) {
		this(problem, n -> null, 0, numberOfCPUs);
	}

	public ForwardDecompositionHTNPlanner(IHTNPlanningProblem problem, INodeEvaluator<TFDNode, V> nodeEvaluator, int timeoutPerNodeFComputation, int numberOfCPUs) {
		this(problem, new ORGraphSearchFactory<>(), nodeEvaluator, numberOfCPUs);
		if (timeoutPerNodeFComputation > 0) {
			((ORGraphSearchFactory<TFDNode, String, V>) this.searchFactory).setTimeoutForFComputation(timeoutPerNodeFComputation, n -> null);
		}
	}

	public ForwardDecompositionHTNPlanner(IHTNPlanningProblem problem, IObservableORGraphSearchFactory<TFDNode, String, V> searchFactory,
			INodeEvaluator<TFDNode, V> nodeEvaluator, int numberOfCPUs) {
		this.planningProblem = problem;
		this.searchFactory = searchFactory;
		this.nodeEvaluator = nodeEvaluator;
		this.numberOfCPUs = numberOfCPUs;
	}

	public int getNumberOfCPUs() {
		return numberOfCPUs;
	}

	@Override
	public SolutionIterator iterator() {
		return new SolutionIterator();
	}

	@Override
	public void registerListener(Object listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public List<Action> getPlan(List<TFDNode> path) {
		return path.stream().filter(n -> n.getAppliedAction() != null).map(n -> n.getAppliedAction()).collect(Collectors.toList());
	}
}
