package jaicore.planning.hierarchical.problems.htn;

import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.planning.core.EvaluatedPlan;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class CostSensitivePlanningToSearchProblemTransformer<IPlanning extends IHTNPlanningProblem, V extends Comparable<V>, N, A>
implements AlgorithmicProblemReduction<CostSensitiveHTNPlanningProblem<IPlanning, V>, EvaluatedPlan<V>, GraphSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>> {

	private final IHierarchicalPlanningGraphGeneratorDeriver<IPlanning, N, A> graphGeneratorDeriver;

	public CostSensitivePlanningToSearchProblemTransformer(final IHierarchicalPlanningGraphGeneratorDeriver<IPlanning, N, A> graphGeneratorDeriver) {
		super();
		this.graphGeneratorDeriver = graphGeneratorDeriver;
	}

	@Override
	public GraphSearchWithPathEvaluationsInput<N, A, V> encodeProblem(final CostSensitiveHTNPlanningProblem<IPlanning, V> problem) {
		IObjectEvaluator<SearchGraphPath<N, A>, V> solutionEvaluator = solutionPath -> problem.getPlanEvaluator().evaluate(CostSensitivePlanningToSearchProblemTransformer.this.graphGeneratorDeriver.decodeSolution(solutionPath));
		return new GraphSearchWithPathEvaluationsInput<>(this.graphGeneratorDeriver.encodeProblem(problem.getCorePlanningProblem()).getGraphGenerator(), solutionEvaluator);
	}

	public IHierarchicalPlanningGraphGeneratorDeriver<IPlanning, N, A> getGraphGeneratorDeriver() {
		return this.graphGeneratorDeriver;
	}

	@Override
	public EvaluatedPlan<V> decodeSolution(final EvaluatedSearchGraphPath<N, A, V> solution) {
		return new EvaluatedPlan<>(this.graphGeneratorDeriver.decodeSolution(solution), solution.getScore());
	}
}
