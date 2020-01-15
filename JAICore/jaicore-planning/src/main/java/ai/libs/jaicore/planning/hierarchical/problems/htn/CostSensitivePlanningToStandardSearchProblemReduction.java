package ai.libs.jaicore.planning.hierarchical.problems.htn;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.basic.algorithm.reduction.IdentityReduction;
import ai.libs.jaicore.planning.core.interfaces.IPlan;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class CostSensitivePlanningToStandardSearchProblemReduction<N, A, V extends Comparable<V>>
extends CostSensitivePlanningToSearchProblemReduction<N, A, V, IHTNPlanningProblem, GraphSearchWithPathEvaluationsInput<N, A, V>, IEvaluatedPath<N, A, V>> {

	public CostSensitivePlanningToStandardSearchProblemReduction(final IHierarchicalPlanningToGraphSearchReduction<N, A, IHTNPlanningProblem, IPlan, GraphSearchInput<N, A>, ILabeledPath<N, A>> baseReduction) {
		super(baseReduction, new IdentityReduction<>());
	}
}
