package jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.IAlgorithmFactory;
import jaicore.planning.classical.problems.ceoc.CEOCAction;
import jaicore.planning.classical.problems.ceoc.CEOCOperation;
import jaicore.planning.core.Action;
import jaicore.planning.core.EvaluatedSearchGraphBasedPlan;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.hierarchical.problems.ceocstn.OCMethod;
import jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import jaicore.planning.hierarchical.testproblems.nesteddichotomies.CEOCSTNNestedDichotomyTest;

public class ForwardDecompositionNestedDichotomyTester extends CEOCSTNNestedDichotomyTest {

	@Override
	public IAlgorithmFactory<CEOCSTNPlanningProblem<CEOCOperation, OCMethod, CEOCAction>, EvaluatedSearchGraphBasedPlan<Action, Double, TFDNode>> getFactory() {
		return new IAlgorithmFactory<CEOCSTNPlanningProblem<CEOCOperation,OCMethod,CEOCAction>, EvaluatedSearchGraphBasedPlan<Action,Double,TFDNode>>() {
			
			private CEOCSTNPlanningProblem<CEOCOperation, OCMethod, CEOCAction> problem;

			@Override
			public <P> void setProblemInput(P problemInput, AlgorithmProblemTransformer<P, CEOCSTNPlanningProblem<CEOCOperation, OCMethod, CEOCAction>> reducer) {
				problem = reducer.transform(problemInput);
			}

			@Override
			public void setProblemInput(CEOCSTNPlanningProblem<CEOCOperation, OCMethod, CEOCAction> problemInput) {
				problem = problemInput;
			}

			@Override
			public IAlgorithm<CEOCSTNPlanningProblem<CEOCOperation, OCMethod, CEOCAction>, EvaluatedSearchGraphBasedPlan<Action, Double, TFDNode>> getAlgorithm() {
				ForwardDecompositionHTNPlannerBasedOnBestFirst algo = new ForwardDecompositionHTNPlannerBasedOnBestFirst<CEOCOperation, OCMethod, CEOCAction, IHTNPlanningProblem<CEOCOperation,OCMethod, CEOCAction>, Double>(problem, n -> 0.0);
				return algo;
			}
			
		};
	}

}
