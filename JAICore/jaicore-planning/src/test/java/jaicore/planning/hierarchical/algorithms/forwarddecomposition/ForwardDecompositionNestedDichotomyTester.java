package jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.IAlgorithmFactory;
import jaicore.planning.core.EvaluatedSearchGraphBasedPlan;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.hierarchical.testproblems.nesteddichotomies.CEOCSTNNestedDichotomyTest;

public class ForwardDecompositionNestedDichotomyTester extends CEOCSTNNestedDichotomyTest {

	@Override
	public IAlgorithmFactory<CEOCSTNPlanningProblem, EvaluatedSearchGraphBasedPlan<Double, TFDNode>> getFactory() {
		return new IAlgorithmFactory<CEOCSTNPlanningProblem, EvaluatedSearchGraphBasedPlan<Double,TFDNode>>() {
			
			private CEOCSTNPlanningProblem problem;

			@Override
			public <P> void setProblemInput(P problemInput, AlgorithmProblemTransformer<P, CEOCSTNPlanningProblem> reducer) {
				problem = reducer.transform(problemInput);
			}

			@Override
			public void setProblemInput(CEOCSTNPlanningProblem problemInput) {
				problem = problemInput;
			}

			@Override
			public IAlgorithm<CEOCSTNPlanningProblem, EvaluatedSearchGraphBasedPlan<Double, TFDNode>> getAlgorithm() {
				ForwardDecompositionHTNPlannerBasedOnBestFirst<CEOCSTNPlanningProblem, Double> algo = new ForwardDecompositionHTNPlannerBasedOnBestFirst<>(problem, n -> 0.0);
				return algo;
			}
			
		};
	}

}
