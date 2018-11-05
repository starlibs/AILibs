package jaicore.planning.model;

import java.util.List;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.planning.graphgenerators.IPlanningGraphGeneratorDeriver;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.planning.model.task.stn.Method;
import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.model.probleminputs.GraphSearchProblemInput;

public class CostSensitivePlanningToSearchProblemTransformer<PO extends Operation, PM extends Method, PA extends Action, I extends IHTNPlanningProblem<PO, PM, PA>, V extends Comparable<V>, N, A>
		implements AlgorithmProblemTransformer<CostSensitiveHTNPlanningProblem<PO, PM, PA, I, V>, GraphSearchProblemInput<N, A, V>> {

	private final IPlanningGraphGeneratorDeriver<PO, PM, PA, I, N, A> graphGeneratorDeriver;

	public CostSensitivePlanningToSearchProblemTransformer(IPlanningGraphGeneratorDeriver<PO, PM, PA, I, N, A> graphGeneratorDeriver) {
		super();
		this.graphGeneratorDeriver = graphGeneratorDeriver;
	}

	@Override
	public GraphSearchProblemInput<N, A, V> transform(CostSensitiveHTNPlanningProblem<PO, PM, PA, I, V> problem) {

		ISolutionEvaluator<N, V> solutionEvaluator = new ISolutionEvaluator<N,V>() {

			@Override
			public V evaluateSolution(List<N> solutionPath) throws Exception {
				return problem.getPlanEvaluator().evaluate(graphGeneratorDeriver.getPlan(solutionPath));
			}

			@Override
			public boolean doesLastActionAffectScoreOfAnySubsequentSolution(List<N> partialSolutionPath) {
				return true;
			}

			@Override
			public void cancel() {
				// TODO Auto-generated method stub
				
			}
		};
		/* derive the concrete graph search problem input */
		return new GraphSearchProblemInput<>(graphGeneratorDeriver.transform(problem.getCorePlanningProblem()), solutionEvaluator);
	}

	public IPlanningGraphGeneratorDeriver<PO, PM, PA, I, N, A> getGraphGeneratorDeriver() {
		return graphGeneratorDeriver;
	}
}
