package jaicore.planning.hierarchical.problems.htn;

import java.util.HashMap;
import java.util.Map;

import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.logging.ToJSONStringUtil;
import jaicore.planning.core.EvaluatedPlan;
import jaicore.search.core.interfaces.ISolutionEvaluator;
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

		ISolutionEvaluator<N, A, V> solutionEvaluator = new ISolutionEvaluator<N, A, V>() {

			@Override
			public V evaluateSolution(final SearchGraphPath<N, A> solutionPath) throws AlgorithmTimeoutedException, InterruptedException, ObjectEvaluationFailedException {
				return problem.getPlanEvaluator().evaluate(CostSensitivePlanningToSearchProblemTransformer.this.graphGeneratorDeriver.decodeSolution(solutionPath));
			}

			@Override
			public boolean doesLastActionAffectScoreOfAnySubsequentSolution(final SearchGraphPath<N, A> partialSolutionPath) {
				return true;
			}

			@Override
			public void cancel() {
				// TODO Auto-generated method stub
			}

			@Override
			public String toString() {
				Map<String, Object> fields = new HashMap<>();
				fields.put("problem", problem);
				return ToJSONStringUtil.toJSONString(fields);
			}
		};
		/* derive the concrete graph search problem input */
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
