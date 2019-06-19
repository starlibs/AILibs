package ai.libs.jaicore.planning.hierarchical.problems.htn;

import ai.libs.jaicore.basic.algorithm.reduction.IdentityReduction;
import ai.libs.jaicore.planning.core.Plan;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class CostSensitivePlanningToStandardSearchProblemReduction<N, A, V extends Comparable<V>>
extends CostSensitivePlanningToSearchProblemReduction<N, A, V, IHTNPlanningProblem, GraphSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>> {

	public CostSensitivePlanningToStandardSearchProblemReduction(final IHierarchicalPlanningToGraphSearchReduction<N, A, IHTNPlanningProblem, Plan, GraphSearchInput<N, A>, SearchGraphPath<N, A>> baseReduction) {
		super(baseReduction, new IdentityReduction<>());
	}
}
