package jaicore.planning.classical.algorithms.strips.forward;

import java.util.List;
import java.util.stream.Collectors;

import jaicore.planning.classical.problems.strips.StripsAction;
import jaicore.planning.classical.problems.strips.StripsPlanningProblem;
import jaicore.planning.core.Plan;
import jaicore.search.core.interfaces.GraphGenerator;

public class STRIPSForwardSearchReducer implements ISTRIPSPlanningGraphGeneratorDeriver<StripsForwardPlanningNode, String> {
	
	@Override
	public GraphGenerator<StripsForwardPlanningNode, String> transform(StripsPlanningProblem problem) {
		return new StripsForwardPlanningGraphGenerator(problem);
	}

	@Override
	public Plan getPlan(List<StripsForwardPlanningNode> path) {
		return new Plan(path.stream().map(n -> (StripsAction)n.getActionToReachState()).filter(a -> a != null).collect(Collectors.toList()));
	}
}
