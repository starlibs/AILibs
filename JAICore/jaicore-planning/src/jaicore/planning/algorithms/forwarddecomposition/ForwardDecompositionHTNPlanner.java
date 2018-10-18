package jaicore.planning.algorithms.forwarddecomposition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.planning.algorithms.IObservableGraphBasedHTNPlanningAlgorithm;
import jaicore.planning.graphgenerators.task.ceociptfd.CEOCIPTFDGraphGenerator;
import jaicore.planning.graphgenerators.task.ceoctfd.CEOCTFDGraphGenerator;
import jaicore.planning.graphgenerators.task.tfd.TFDGraphGenerator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.planning.model.task.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.model.task.stn.Method;
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
public class ForwardDecompositionHTNPlanner<V extends Comparable<V>> implements IObservableGraphBasedHTNPlanningAlgorithm<ForwardDecompositionSolution, TFDNode, String, V>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(ForwardDecompositionHTNPlanner.class);

	/* core elements of the search */
	private SolutionIterator iterator;
	private boolean canceled = false;
	private final IHTNPlanningProblem planningProblem;
	private final GraphGenerator<TFDNode, String> graphGenerator;
	private final IObservableORGraphSearchFactory<TFDNode, String, V> searchFactory;
	private final INodeEvaluator<TFDNode, V> nodeEvaluator;
	private String loggerName;

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

	public ForwardDecompositionHTNPlanner(IHTNPlanningProblem problem, IObservableORGraphSearchFactory<TFDNode, String, V> searchFactory, INodeEvaluator<TFDNode, V> nodeEvaluator, int numberOfCPUs) {
		this.planningProblem = problem;
		this.searchFactory = searchFactory;
		this.nodeEvaluator = nodeEvaluator;
		this.numberOfCPUs = numberOfCPUs;

		/* derive the graph generator for this planning problem */
		if (planningProblem instanceof CEOCIPSTNPlanningProblem) {
			graphGenerator = new CEOCIPTFDGraphGenerator((CEOCIPSTNPlanningProblem) planningProblem);
		} else if (planningProblem instanceof CEOCSTNPlanningProblem) {
			graphGenerator = new CEOCTFDGraphGenerator((CEOCSTNPlanningProblem) planningProblem);
		} else if (planningProblem.getClass().equals(STNPlanningProblem.class)) {
			graphGenerator = new TFDGraphGenerator((STNPlanningProblem) planningProblem);
		} else {
			throw new IllegalArgumentException("HTN problems of class \"" + planningProblem.getClass().getName() + "\" are currently not supported.");
		}
	}

	public int getNumberOfCPUs() {
		return numberOfCPUs;
	}

	@Override
	public SolutionIterator iterator() {
		if (iterator != null)
			throw new UnsupportedOperationException("ForwardDecomposition allows only to draw one iterator.");
		if (this.canceled)
			throw new IllegalStateException("The planning process has already been canceled. We cannot generate an iterator afterwards.");
		iterator = new SolutionIterator();
		return iterator;
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

	public Map<String, Object> getAnnotationsOfSolution(ForwardDecompositionSolution solution) {
		return iterator.getSolutionAnnotations(solution);
	}

	@Override
	public void cancel() {
		this.canceled = true;
		iterator.getSearch().cancel();
	}

	@Override
	public void setLoggerName(String name) {
		this.logger.info("Switching logger from {} to {}", logger.getName(), name);
		this.loggerName = name;
		logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, logger.getName());
	}

	@Override
	public String getLoggerName() {
		return loggerName;
	}

	@Override
	public GraphGenerator<TFDNode, String> getGraphGenerator() {
		return graphGenerator;
	}

	public class SolutionIterator implements Iterator<ForwardDecompositionSolution> {

		private boolean initialized = false;
		private boolean moreSolutionsMightExist = true;
		private IObservableORGraphSearch<TFDNode, String, V> search;
		private ForwardDecompositionSolution nextSolution;

		public SolutionIterator() {
			search = searchFactory.createSearch(graphGenerator, nodeEvaluator, numberOfCPUs);
		}

		@Override
		public boolean hasNext() {
			logger.info("I'm being asked whether there is a next solution.");
			if (!initialized) {
				logger.info("Starting HTN planning process. The graph generator is {}", search.getGraphGenerator());
				if (logger.isDebugEnabled()) {
					StringBuilder opSB = new StringBuilder();
					for (Operation op : planningProblem.getDomain().getOperations()) {
						opSB.append("\n\t\t");
						opSB.append(op);
					}
					StringBuilder methodSB = new StringBuilder();
					for (Method method : planningProblem.getDomain().getMethods()) {
						methodSB.append("\n\t\t");
						methodSB.append(method);
					}
					logger.debug("The HTN problem is defined as follows:\n\tOperations:{}\n\tMethods:{}", opSB.toString(), methodSB.toString());
				}
				synchronized (listeners) {
					for (Object listener : listeners) {
						search.registerListener(listener);
					}
				}
				if (loggerName != null && loggerName.length() > 0 && search instanceof ILoggingCustomizable) {
					logger.info("Customizing logger of search with {}", loggerName);
					((ILoggingCustomizable) search).setLoggerName(loggerName + ".search");
				}
				initialized = true;
			}
			if (canceled)
				throw new IllegalStateException("The planner has already been canceled. Cannot compute more plans.");
			if (!moreSolutionsMightExist) {
				logger.info("No more solutions will be found.");
				return false;
			}
			logger.info("Starting/continuing search for next plan.");
			try {
				List<TFDNode> solution = search.nextSolution();
				if (solution == null) {
					logger.info("No solution found. Concluding that no more solutions can be found.");
					moreSolutionsMightExist = false;
					return false;
				}
				logger.info("Next solution found.");
				List<Action> plan = solution.stream().filter(n -> n.getAppliedAction() != null).map(n -> n.getAppliedAction()).collect(Collectors.toList());
				nextSolution = new ForwardDecompositionSolution(plan, solution);
				return true;
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
		}

		@Override
		public ForwardDecompositionSolution next() {
			if (!moreSolutionsMightExist || (nextSolution == null && !hasNext()))
				throw new NoSuchElementException();
			if (nextSolution.getPlan() == null) {
				throw new IllegalStateException("Planner has no more solution even though hasNext said that there would be one.");
			}
			ForwardDecompositionSolution solutionToReturn = nextSolution;
			nextSolution = null;
			return solutionToReturn;
		}

		public Map<String, Object> getSolutionAnnotations(ForwardDecompositionSolution solution) {
			return search.getAnnotationsOfReturnedSolution(solution.getPath());
		}

		public IObservableORGraphSearch<TFDNode, String, V> getSearch() {
			return search;
		}
	}
}
