package jaicore.planning.algorithms.strips;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.sets.SetUtil;
import jaicore.graphvisualizer.TooltipGenerator;
import jaicore.graphvisualizer.gui.VisualizationWindow;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.EvaluatedPlan;
import jaicore.planning.algorithms.IPlanningAlgorithm;
import jaicore.planning.algorithms.events.PlanFoundEvent;
import jaicore.planning.graphgenerators.strips.forward.StripsForwardPlanningNode;
import jaicore.planning.graphgenerators.strips.forward.StripsTooltipGenerator;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.core.Plan;
import jaicore.planning.model.strips.StripsAction;
import jaicore.planning.model.strips.StripsOperation;
import jaicore.planning.model.strips.StripsPlanningProblem;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.travesaltree.Node;

public class STRIPSPlanner<V extends Comparable<V>>
		extends IPlanningAlgorithm<StripsPlanningProblem, EvaluatedPlan<StripsAction, V>, V> {
	private IGraphSearch<GeneralEvaluatedTraversalTree<StripsForwardPlanningNode, String, V>, EvaluatedSearchGraphPath<StripsForwardPlanningNode, String, V>, StripsForwardPlanningNode, String, V, Node<StripsForwardPlanningNode, V>, String> search;
	private final INodeEvaluator<StripsForwardPlanningNode, V> nodeEvaluator;
	private final STRIPSForwardSearchReducer reducer = new STRIPSForwardSearchReducer();

	/* state of the algorithm */
	private boolean visualize = false;

	public STRIPSPlanner(StripsPlanningProblem problem, INodeEvaluator<StripsForwardPlanningNode, V> nodeEvaluator) {
		super(problem);
		this.nodeEvaluator = nodeEvaluator;

		/* conduct some consistency checks */
		if (!problem.getInitState().getVariableParams().isEmpty())
			throw new IllegalArgumentException(
					"The initial state contains variable parameters but must only contain constants!\nList of found variables: "
							+ problem.getInitState().getVariableParams().stream().map(n -> "\n\t" + n.getName())
									.collect(Collectors.joining()));
		if (!problem.getGoalState().getVariableParams().isEmpty())
			throw new IllegalArgumentException(
					"The goal state contains variable parameters but must only contain constants!\nList of found variables: "
							+ problem.getGoalState().getVariableParams().stream().map(n -> "\n\t" + n.getName())
									.collect(Collectors.joining()));

		/*
		 * check that every operation has only arguments in its preconditions, add lists
		 * and delete lists, that are also explicitly defined in the param list
		 */
		for (Operation o : problem.getDomain().getOperations()) {
			StripsOperation so = (StripsOperation) o;
			Collection<VariableParam> undeclaredParamsInPrecondition = SetUtil
					.difference(so.getPrecondition().getVariableParams(), so.getParams());
			if (!undeclaredParamsInPrecondition.isEmpty())
				throw new IllegalArgumentException("The precondition of operation " + so.getName()
						+ " contains variables that are not defined in the parameter list: "
						+ undeclaredParamsInPrecondition);
			Collection<VariableParam> undeclaredParamsInAddList = SetUtil
					.difference(so.getAddList().getVariableParams(), so.getParams());
			if (!undeclaredParamsInAddList.isEmpty())
				throw new IllegalArgumentException("The add list of operation " + so.getName()
						+ " contains variables that are not defined in the parameter list: "
						+ undeclaredParamsInAddList);
			Collection<VariableParam> undeclaredParamsInDelList = SetUtil
					.difference(so.getDeleteList().getVariableParams(), so.getParams());
			if (!undeclaredParamsInDelList.isEmpty())
				throw new IllegalArgumentException("The del list of operation " + so.getName()
						+ " contains variables that are not defined in the parameter list: "
						+ undeclaredParamsInDelList);
		}
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmExecutionCanceledException, InterruptedException {
		switch (getState()) {
		case created:
			setState(AlgorithmState.active);

			/* logging problem */
			StripsPlanningProblem problem = getInput();
			getLogger().info(
					"Initializing planner for the following problem:\n\tOperations:{}\n\tInitial State: {}\n\tGoal State: {}",
					problem.getDomain().getOperations().stream()
							.map(o -> "\n\t - " + o.getName() + "\n\t\tParams: " + o.getParams() + "\n\t\tPre: "
									+ o.getPrecondition() + "\n\t\tAdd: " + ((StripsOperation) o).getAddList()
									+ "\n\t\tDel: " + ((StripsOperation) o).getDeleteList())
							.collect(Collectors.joining()),
					problem.getInitState(), problem.getGoalState());

			/* create search algorithm */
			GraphGenerator<StripsForwardPlanningNode, String> gg = reducer.transform(problem);
			GeneralEvaluatedTraversalTree<StripsForwardPlanningNode, String, V> searchProblem = new GeneralEvaluatedTraversalTree<>(
					gg, nodeEvaluator);
			search = new BestFirst<GeneralEvaluatedTraversalTree<StripsForwardPlanningNode, String, V>, StripsForwardPlanningNode, String, V>(
					searchProblem);
			search.setTimeout(getTimeout());
			setLoggerOfSearch();
			if (visualize) {
				VisualizationWindow<Node<StripsForwardPlanningNode, V>, String> w = new VisualizationWindow<>(search);
				TooltipGenerator<StripsForwardPlanningNode> tt = new StripsTooltipGenerator<>();
				w.setTooltipGenerator(n -> tt.getTooltip(((Node<StripsForwardPlanningNode, V>) n).getPoint()));
			}
			return new AlgorithmInitializedEvent();
		case active:

			/* invoke next step in search algorithm */
			if (!search.hasNext()) {
				setState(AlgorithmState.inactive);
				return new AlgorithmFinishedEvent();
			}
			try {
				EvaluatedSearchGraphPath<StripsForwardPlanningNode, String, V> nextSolution = search.nextSolution();
				Plan<StripsAction> plan = reducer.getPlan(nextSolution.getNodes());
				EvaluatedPlan<StripsAction, V> evaluatedPlan = new EvaluatedPlan<>(plan, nextSolution.getScore());
				updateBestSeenSolution(evaluatedPlan);
				return new PlanFoundEvent<>(evaluatedPlan);
			} catch (NoSuchElementException e) {
				setState(AlgorithmState.active);
				return new AlgorithmFinishedEvent();
			}

		default:
			throw new IllegalStateException("Cannot handle algorithm state " + getState());
		}
	}

	public void enableVisualization() {
		visualize = true;
	}

	@Override
	public void cancel() {
		super.cancel();
		if (search != null)
			search.cancel();
	}

	@Override
	public Map<String, Object> getAnnotationsOfSolution(EvaluatedPlan<StripsAction, V> solution) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLoggerName(String name) {
		super.setLoggerName(name);
		setLoggerOfSearch();
	}

	private void setLoggerOfSearch() {
		if (this.search instanceof ILoggingCustomizable) {
			getLogger().info("Switching logger of search to {}.search", getLoggerName());
			((ILoggingCustomizable) this.search).setLoggerName(getLoggerName() + ".search");
		} else if (search != null)
			getLogger().info("The search is of class {}, which is not logging customizable.", this.search.getClass());
	}

	@Override
	public EvaluatedPlan<StripsAction, V> getOutput() {
		return getBestSeenSolution();
	}
}
