package jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.IAlgorithmFactory;
import jaicore.planning.classical.problems.ceoc.CEOCAction;
import jaicore.planning.core.EvaluatedSearchGraphBasedPlan;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningProblem;

//public class ForwardDecompositionNestedDichotomyTester extends CEOCSTNNestedDichotomyTest {
public class ForwardDecompositionNestedDichotomyTester {
	
	@Test
	public void f() {
		final StaticLoggerBinder binder = StaticLoggerBinder.getSingleton();
		System.out.println(binder.getLoggerFactory());
		System.out.println(binder.getLoggerFactoryClassStr());
		Logger logger = LoggerFactory.getLogger("testedalgorithm.search");
		logger.info("OK");
	}
	
	public IAlgorithmFactory<CEOCSTNPlanningProblem, EvaluatedSearchGraphBasedPlan<CEOCAction, Double, TFDNode>> getFactory() {
		return new IAlgorithmFactory<CEOCSTNPlanningProblem, EvaluatedSearchGraphBasedPlan<CEOCAction,Double,TFDNode>>() {
			
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
			public IAlgorithm<CEOCSTNPlanningProblem, EvaluatedSearchGraphBasedPlan<CEOCAction, Double, TFDNode>> getAlgorithm() {
				ForwardDecompositionHTNPlannerBasedOnBestFirst<CEOCAction, CEOCSTNPlanningProblem, Double> algo = new ForwardDecompositionHTNPlannerBasedOnBestFirst<>(problem, n -> 0.0);
				return algo;
			}
			
		};
	}

}
