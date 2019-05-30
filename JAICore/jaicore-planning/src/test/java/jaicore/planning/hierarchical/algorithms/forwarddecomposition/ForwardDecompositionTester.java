package jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import jaicore.planning.tests.HTNPlanningAlgorithmTester;

public class ForwardDecompositionTester extends HTNPlanningAlgorithmTester {

	@Override
	public IAlgorithm<?, ?> getPlanningAlgorithm(final IHTNPlanningProblem planningProblem) {
		return new ForwardDecompositionHTNPlannerBasedOnBestFirst<>(planningProblem, n -> 0.0);
	}
}
