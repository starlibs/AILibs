package ai.libs.jaicore.planning.hierarchical.problems.htn;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.basic.algorithm.reduction.IdentityReduction;
import ai.libs.jaicore.planning.core.interfaces.IPlan;

public class CostSensitivePlanningToStandardSearchProblemReduction<I extends IHTNPlanningProblem, N, A, V extends Comparable<V>>
extends CostSensitivePlanningToSearchProblemReduction<N, A, V, I, IPathSearchWithPathEvaluationsInput<N, A, V>, IEvaluatedPath<N, A, V>> {

	public CostSensitivePlanningToStandardSearchProblemReduction(final IHierarchicalPlanningToGraphSearchReduction<N, A, I, IPlan, ? extends IPathSearchInput<N, A>, ILabeledPath<N, A>> baseReduction) {
		super(baseReduction, new IdentityReduction<>());
	}
}
