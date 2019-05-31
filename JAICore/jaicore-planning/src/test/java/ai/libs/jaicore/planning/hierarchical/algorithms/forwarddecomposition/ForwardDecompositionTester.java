package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import ai.libs.jaicore.basic.algorithm.IAlgorithm;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.ForwardDecompositionHTNPlannerBasedOnBestFirst;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import ai.libs.jaicore.planning.tests.HTNPlanningAlgorithmTester;

public class ForwardDecompositionTester extends HTNPlanningAlgorithmTester {

	@Override
	public IAlgorithm<?, ?> getPlanningAlgorithm(final IHTNPlanningProblem planningProblem) {
		return new ForwardDecompositionHTNPlannerBasedOnBestFirst<>(planningProblem, n -> 0.0);
	}
}
