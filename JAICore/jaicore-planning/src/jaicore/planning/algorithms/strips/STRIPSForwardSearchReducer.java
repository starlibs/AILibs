package jaicore.planning.algorithms.strips;

import java.util.List;
import java.util.stream.Collectors;

import jaicore.planning.graphgenerators.ISTRIPSPlanningGraphGeneratorDeriver;
import jaicore.planning.graphgenerators.strips.forward.StripsForwardPlanningGraphGenerator;
import jaicore.planning.graphgenerators.strips.forward.StripsForwardPlanningNode;
import jaicore.planning.model.core.Plan;
import jaicore.planning.model.strips.StripsAction;
import jaicore.planning.model.strips.StripsPlanningProblem;
import jaicore.search.core.interfaces.GraphGenerator;

public class STRIPSForwardSearchReducer implements ISTRIPSPlanningGraphGeneratorDeriver<StripsAction, StripsForwardPlanningNode, String> {
	
	@Override
	public GraphGenerator<StripsForwardPlanningNode, String> transform(StripsPlanningProblem problem) {
		return new StripsForwardPlanningGraphGenerator(problem);
	}

	@Override
	public Plan<StripsAction> getPlan(List<StripsForwardPlanningNode> path) {
		return new Plan<>(path.stream().map(n -> (StripsAction)n.getActionToReachState()).filter(a -> a != null).collect(Collectors.toList()));
	}
}
