package jaicore.planning.hierarchical.problems.htn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.logging.ToJSONStringUtil;
import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class CostSensitivePlanningToSearchProblemTransformer<IPlanning extends IHTNPlanningProblem, V extends Comparable<V>, N, A>
		implements AlgorithmicProblemReduction<CostSensitiveHTNPlanningProblem<IPlanning, V>, GraphSearchWithPathEvaluationsInput<N, A, V>> {

	private final IHierarchicalPlanningGraphGeneratorDeriver<IPlanning, N, A> graphGeneratorDeriver;

	public CostSensitivePlanningToSearchProblemTransformer(final IHierarchicalPlanningGraphGeneratorDeriver<IPlanning, N, A> graphGeneratorDeriver) {
		super();
		this.graphGeneratorDeriver = graphGeneratorDeriver;
	}

	@Override
	public GraphSearchWithPathEvaluationsInput<N, A, V> transform(final CostSensitiveHTNPlanningProblem<IPlanning, V> problem) {

		ISolutionEvaluator<N, V> solutionEvaluator = new ISolutionEvaluator<N, V>() {

			@Override
			public V evaluateSolution(final List<N> solutionPath) throws TimeoutException, InterruptedException, ObjectEvaluationFailedException {
				return problem.getPlanEvaluator().evaluate(CostSensitivePlanningToSearchProblemTransformer.this.graphGeneratorDeriver.getPlan(solutionPath));
			}

			@Override
			public boolean doesLastActionAffectScoreOfAnySubsequentSolution(final List<N> partialSolutionPath) {
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
		return new GraphSearchWithPathEvaluationsInput<>(this.graphGeneratorDeriver.encodeProblem(problem.getCorePlanningProblem()), solutionEvaluator);
	}

	public IHierarchicalPlanningGraphGeneratorDeriver<IPlanning, N, A> getGraphGeneratorDeriver() {
		return this.graphGeneratorDeriver;
	}
}
