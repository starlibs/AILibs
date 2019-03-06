package jaicore.planning.classical.algorithms.strips.forward;

import java.util.Objects;
import java.util.stream.Collectors;

import jaicore.planning.classical.problems.strips.StripsAction;
import jaicore.planning.classical.problems.strips.StripsPlanningProblem;
import jaicore.planning.core.Plan;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.other.SearchGraphPath;

public class STRIPSForwardSearchReducer implements ISTRIPSPlanningGraphGeneratorDeriver<StripsForwardPlanningNode, String> {

	@Override
	public GraphGenerator<StripsForwardPlanningNode, String> encodeProblem(final StripsPlanningProblem problem) {
		return new StripsForwardPlanningGraphGenerator(problem);
	}

	@Override
	public Plan decodeSolution(final SearchGraphPath<StripsForwardPlanningNode, String> solution) {
		return new Plan(solution.getNodes().stream().map(n -> (StripsAction)n.getActionToReachState()).filter(Objects::nonNull).collect(Collectors.toList()));
	}
}
