package jaicore.planning.classical.algorithms.strips.forward;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.AOptimizer;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.sets.SetUtil;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.classical.problems.strips.Operation;
import jaicore.planning.classical.problems.strips.StripsOperation;
import jaicore.planning.classical.problems.strips.StripsPlanningProblem;
import jaicore.planning.core.EvaluatedPlan;
import jaicore.planning.core.Plan;
import jaicore.planning.core.events.PlanFoundEvent;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.IPathInORGraphSearch;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class STRIPSPlanner<V extends Comparable<V>> extends AOptimizer<StripsPlanningProblem, EvaluatedPlan<V>, V> {

	/* logging */
	private Logger logger = LoggerFactory.getLogger(BestFirst.class);
	private String loggerName;

	private final IPathInORGraphSearch<GraphSearchWithSubpathEvaluationsInput<StripsForwardPlanningNode, String, V>, EvaluatedSearchGraphPath<StripsForwardPlanningNode, String, V>, StripsForwardPlanningNode, String> search;
	private final INodeEvaluator<StripsForwardPlanningNode, V> nodeEvaluator;
	private final STRIPSForwardSearchReducer reducer = new STRIPSForwardSearchReducer();

	/* state of the algorithm */
	private boolean visualize = false;
	private final GraphGenerator<StripsForwardPlanningNode, String> graphGenerator;

	public STRIPSPlanner(final StripsPlanningProblem problem, final INodeEvaluator<StripsForwardPlanningNode, V> nodeEvaluator) {
		super(problem);
		this.nodeEvaluator = nodeEvaluator;

		/* conduct some consistency checks */
		if (!problem.getInitState().getVariableParams().isEmpty()) {
			throw new IllegalArgumentException("The initial state contains variable parameters but must only contain constants!\nList of found variables: "
					+ problem.getInitState().getVariableParams().stream().map(n -> "\n\t" + n.getName()).collect(Collectors.joining()));
		}
		if (!problem.getGoalState().getVariableParams().isEmpty()) {
			throw new IllegalArgumentException("The goal state contains variable parameters but must only contain constants!\nList of found variables: "
					+ problem.getGoalState().getVariableParams().stream().map(n -> "\n\t" + n.getName()).collect(Collectors.joining()));
		}

		/*
		 * check that every operation has only arguments in its preconditions, add lists
		 * and delete lists, that are also explicitly defined in the param list
		 */
		for (Operation o : problem.getDomain().getOperations()) {
			StripsOperation so = (StripsOperation) o;
			Collection<VariableParam> undeclaredParamsInPrecondition = SetUtil.difference(so.getPrecondition().getVariableParams(), so.getParams());
			if (!undeclaredParamsInPrecondition.isEmpty()) {
				throw new IllegalArgumentException("The precondition of operation " + so.getName() + " contains variables that are not defined in the parameter list: " + undeclaredParamsInPrecondition);
			}
			Collection<VariableParam> undeclaredParamsInAddList = SetUtil.difference(so.getAddList().getVariableParams(), so.getParams());
			if (!undeclaredParamsInAddList.isEmpty()) {
				throw new IllegalArgumentException("The add list of operation " + so.getName() + " contains variables that are not defined in the parameter list: " + undeclaredParamsInAddList);
			}
			Collection<VariableParam> undeclaredParamsInDelList = SetUtil.difference(so.getDeleteList().getVariableParams(), so.getParams());
			if (!undeclaredParamsInDelList.isEmpty()) {
				throw new IllegalArgumentException("The del list of operation " + so.getName() + " contains variables that are not defined in the parameter list: " + undeclaredParamsInDelList);
			}
		}

		/* logging problem */
		if (this.logger.isInfoEnabled()) { // have explicit check here, because we have so many computations in the argument of the info call
			this.logger.info("Initializing planner for the following problem:\n\tOperations:{}\n\tInitial State: {}\n\tGoal State: {}",
					problem.getDomain().getOperations().stream()
					.map(o -> "\n\t - " + o.getName() + "\n\t\tParams: " + o.getParams() + "\n\t\tPre: " + o.getPrecondition() + "\n\t\tAdd: " + ((StripsOperation) o).getAddList() + "\n\t\tDel: " + ((StripsOperation) o).getDeleteList())
					.collect(Collectors.joining()),
					problem.getInitState(), problem.getGoalState());
		}

		/* create search algorithm */
		this.graphGenerator = this.reducer.encodeProblem(problem);
		GraphSearchWithSubpathEvaluationsInput<StripsForwardPlanningNode, String, V> searchProblem = new GraphSearchWithSubpathEvaluationsInput<>(this.graphGenerator, nodeEvaluator);
		this.search = new BestFirst<>(searchProblem);
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmExecutionCanceledException, InterruptedException, AlgorithmTimeoutedException, AlgorithmException {
		switch (this.getState()) {
		case CREATED:
			this.setLoggerOfSearch();
			this.search.setTimeout(this.getTimeout());
			if (this.visualize) {
				throw new UnsupportedOperationException("Currently no visualization supported!");
			}
			return this.activate();
		case ACTIVE:

			/* invoke next step in search algorithm */
			if (!this.search.hasNext()) {
				return this.terminate();
			}
			try {
				EvaluatedSearchGraphPath<StripsForwardPlanningNode, String, V> nextSolution = this.search.nextSolutionCandidate();
				Plan plan = this.reducer.decodeSolution(nextSolution);
				EvaluatedPlan<V> evaluatedPlan = new EvaluatedPlan<>(plan, nextSolution.getScore());
				this.updateBestSeenSolution(evaluatedPlan);
				return new PlanFoundEvent<>(this.getId(), evaluatedPlan);
			} catch (NoSuchElementException e) {
				return this.terminate();
			}

		default:
			throw new IllegalStateException("Cannot handle algorithm state " + this.getState());
		}
	}

	public void enableVisualization() {
		this.visualize = true;
	}

	@Override
	public void cancel() {
		super.cancel();
		if (this.search != null) {
			this.search.cancel();
		}
	}

	private void setLoggerOfSearch() {
		if (this.search != null && this.loggerName != null) {
			if (this.search instanceof ILoggingCustomizable) {
				this.logger.info("Switching logger of search to {}.search", this.getLoggerName());
				((ILoggingCustomizable) this.search).setLoggerName(this.getLoggerName() + ".search");
			} else {
				this.logger.info("The search is of class {}, which is not logging customizable.", this.search.getClass());
			}
		} else {
			this.logger.info("Not yet setting logger of search, since search has not yet been configured.");
		}
	}

	public GraphGenerator<StripsForwardPlanningNode, String> getGraphGenerator() {
		return this.graphGenerator;
	}

	@Override
	public void setLoggerName(final String name) {
		if (name == null) {
			throw new IllegalArgumentException("Logger name must not be set to null.");
		}
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		if (this.nodeEvaluator instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.nodeEvaluator).setLoggerName(name + ".nodeeval");
		}
		this.setLoggerOfSearch();
		super.setLoggerName(this.loggerName + "._planningalgorithm");
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}
}
