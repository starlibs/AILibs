package jaicore.planning.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.logging.ToJSONStringUtil;
import jaicore.planning.graphgenerators.IHierarchicalPlanningGraphGeneratorDeriver;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.planning.model.task.stn.Method;
import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class CostSensitivePlanningToSearchProblemTransformer<PO extends Operation, PM extends Method, PA extends Action, I extends IHTNPlanningProblem<PO, PM, PA>, V extends Comparable<V>, N, A>
		implements AlgorithmProblemTransformer<CostSensitiveHTNPlanningProblem<PO, PM, PA, I, V>, GraphSearchWithPathEvaluationsInput<N, A, V>> {

	private final IHierarchicalPlanningGraphGeneratorDeriver<PO, PM, PA, I, N, A> graphGeneratorDeriver;

	public CostSensitivePlanningToSearchProblemTransformer(final IHierarchicalPlanningGraphGeneratorDeriver<PO, PM, PA, I, N, A> graphGeneratorDeriver) {
		super();
		this.graphGeneratorDeriver = graphGeneratorDeriver;
	}

	@Override
	public GraphSearchWithPathEvaluationsInput<N, A, V> transform(final CostSensitiveHTNPlanningProblem<PO, PM, PA, I, V> problem) {

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
		return new GraphSearchWithPathEvaluationsInput<>(this.graphGeneratorDeriver.transform(problem.getCorePlanningProblem()), solutionEvaluator);
	}

	public IHierarchicalPlanningGraphGeneratorDeriver<PO, PM, PA, I, N, A> getGraphGeneratorDeriver() {
		return this.graphGeneratorDeriver;
	}
}
