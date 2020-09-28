package ai.libs.jaicore.planning.hierarchical.problems.htn;



import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.planning.core.EvaluatedPlan;
import ai.libs.jaicore.planning.core.interfaces.IEvaluatedPlan;
import ai.libs.jaicore.planning.core.interfaces.IPlan;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class CostSensitivePlanningToSearchProblemReduction<N, A, V extends Comparable<V>, I1 extends IHTNPlanningProblem, I2 extends IPathSearchWithPathEvaluationsInput<N, A, V>, O2 extends IEvaluatedPath<N, A, V>>
implements IHierarchicalPlanningToGraphSearchReduction<N, A, CostSensitiveHTNPlanningProblem<I1, V>, IEvaluatedPlan<V>, I2, O2> {

	private final IHierarchicalPlanningToGraphSearchReduction<N, A, I1, IPlan, ? extends IPathSearchInput<N, A>, ILabeledPath<N, A>> baseReduction;
	private final AlgorithmicProblemReduction<? super IPathSearchWithPathEvaluationsInput<N, A, V>, ? super EvaluatedSearchGraphPath<N, A, V>, I2, O2> forwardReduction;

	public CostSensitivePlanningToSearchProblemReduction(final IHierarchicalPlanningToGraphSearchReduction<N, A, I1, IPlan, ? extends IPathSearchInput<N, A>, ILabeledPath<N, A>> baseReduction,
			final AlgorithmicProblemReduction<? super IPathSearchWithPathEvaluationsInput<N, A, V>, ? super EvaluatedSearchGraphPath<N, A, V>, I2, O2> forwardReduction) {
		super();
		this.baseReduction = baseReduction;
		this.forwardReduction = forwardReduction;
	}

	/**
	 * This method operates in three steps:
	 * 1) it derives a general graph search problem from the given planning problem
	 * 2) it combines the obtained graph search problem with a path evaluation function into a GrahhSearchWithPathEvaluationsInput
	 * 3) it derives a potentially more informed GraphSearchInput
	 *
	 * The last process is called the forward reduction. The output does not change.
	 */
	@Override
	public I2 encodeProblem(final CostSensitiveHTNPlanningProblem<I1, V> problem) {
		return this.forwardReduction.encodeProblem(new GraphSearchWithPathEvaluationsInput<>(this.baseReduction.encodeProblem(problem.getCorePlanningProblem()), new PlanEvaluationBasedSearchEvaluator<>(problem.getPlanEvaluator(), this.baseReduction)));
	}

	@Override
	public IEvaluatedPlan<V> decodeSolution(final O2 solution) {
		return new EvaluatedPlan<>(this.baseReduction.decodeSolution(solution), solution.getScore());
	}
}
