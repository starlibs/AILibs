package ai.libs.jaicore.planning.classical.algorithms.strips.forward;

import java.util.Collection;
import java.util.stream.Collectors;

import org.api4.java.algorithm.IAlgorithmFactory;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.logic.fol.structure.VariableParam;
import ai.libs.jaicore.planning.classical.problems.strips.Operation;
import ai.libs.jaicore.planning.classical.problems.strips.StripsOperation;
import ai.libs.jaicore.planning.classical.problems.strips.StripsPlanningProblem;
import ai.libs.jaicore.planning.core.interfaces.IGraphSearchBasedPlan;
import ai.libs.jaicore.planning.hierarchical.algorithms.GraphSearchBasedPlanningAlgorithm;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public class STRIPSPlanner<V extends Comparable<V>> extends GraphSearchBasedPlanningAlgorithm<StripsPlanningProblem, IGraphSearchBasedPlan<StripsForwardPlanningNode, String>, GraphSearchInput<StripsForwardPlanningNode, String>, SearchGraphPath<StripsForwardPlanningNode, String>, StripsForwardPlanningNode, String> {

	public STRIPSPlanner(final StripsPlanningProblem problem,
			final AlgorithmicProblemReduction<StripsPlanningProblem, IGraphSearchBasedPlan<StripsForwardPlanningNode, String>, GraphSearchInput<StripsForwardPlanningNode, String>, SearchGraphPath<StripsForwardPlanningNode, String>> problemTransformer,
			final IAlgorithmFactory<GraphSearchInput<StripsForwardPlanningNode, String>, SearchGraphPath<StripsForwardPlanningNode, String>, ?> baseFactory) {
		super(problem, problemTransformer, baseFactory);
	}

	@Override
	public void runPreCreationHook() {
		StripsPlanningProblem problem = this.getInput();

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
		if (this.getLogger().isInfoEnabled()) { // have explicit check here, because we have so many computations in the argument of the info call
			this.getLogger().info("Initializing planner for the following problem:\n\tOperations:{}\n\tInitial State: {}\n\tGoal State: {}",
					problem.getDomain().getOperations().stream()
					.map(o -> "\n\t - " + o.getName() + "\n\t\tParams: " + o.getParams() + "\n\t\tPre: " + o.getPrecondition() + "\n\t\tAdd: " + ((StripsOperation) o).getAddList() + "\n\t\tDel: " + ((StripsOperation) o).getDeleteList())
					.collect(Collectors.joining()),
					problem.getInitState(), problem.getGoalState());
		}
	}
}
