package util.planning.graphgenerators.strips.forward;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import util.logic.Monom;
import util.planning.model.core.PlannerUtil;
import util.planning.model.strips.StripsAction;
import util.planning.model.strips.StripsPlanningDomain;
import util.planning.model.strips.StripsPlanningProblem;
import util.search.core.NodeExpansionDescription;
import util.search.core.NodeType;
import util.search.core.GraphGenerator;
import util.search.graphgenerator.GoalTester;
import util.search.graphgenerator.RootGenerator;
import util.search.graphgenerator.SuccessorGenerator;

public class StripsForwardPlanningGraphGenerator implements GraphGenerator<StripsForwardPlanningNode,String> {

	private final StripsPlanningProblem problem;
		
	public StripsForwardPlanningGraphGenerator(StripsPlanningProblem problem) {
		this.problem = problem;
	}

	@Override
	public RootGenerator<StripsForwardPlanningNode> getRootGenerator() {
		return () -> Arrays.asList(new StripsForwardPlanningNode[]{ new StripsForwardPlanningNode(problem.getInitState(), null)});
	}

	@Override
	public SuccessorGenerator<StripsForwardPlanningNode,String> getSuccessorGenerator() {
		return l -> {
			List<NodeExpansionDescription<StripsForwardPlanningNode,String>> successors = new ArrayList<>();
			Monom state = l.getPoint().getState();
			for (StripsAction action : PlannerUtil.getApplicableActionsInState(state, (StripsPlanningDomain)problem.getDomain())) {
				Monom successorState = new Monom(state);
				successorState.removeAll(action.getDeleteList());
				successorState.addAll(action.getAddList());
				successors.add(new NodeExpansionDescription<>(l.getPoint(), new StripsForwardPlanningNode(successorState, action), "edge label", NodeType.OR));
			}
			return successors;
		};
	}

	@Override
	public GoalTester<StripsForwardPlanningNode> getGoalTester() {
		return l -> l.getPoint().getState().containsAll(problem.getGoalState());
	}
}
