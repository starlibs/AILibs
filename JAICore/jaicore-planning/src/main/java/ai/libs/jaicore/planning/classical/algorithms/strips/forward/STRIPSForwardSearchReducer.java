package ai.libs.jaicore.planning.classical.algorithms.strips.forward;

import java.util.Objects;
import java.util.stream.Collectors;

import ai.libs.jaicore.planning.classical.problems.strips.StripsAction;
import ai.libs.jaicore.planning.classical.problems.strips.StripsPlanningProblem;
import ai.libs.jaicore.planning.core.Plan;
import ai.libs.jaicore.search.core.interfaces.GraphGenerator;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

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
