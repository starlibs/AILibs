package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import org.api4.java.algorithm.IOptimizationAlgorithm;

import ai.libs.jaicore.planning.hierarchical.problems.htn.UniformCostHTNPlanningProblem;
import ai.libs.jaicore.planning.tests.OptimizingHTNPlanningAlgorithmTester;

public class ForwardDecompositionTester extends OptimizingHTNPlanningAlgorithmTester {

	@Override
	public IOptimizationAlgorithm<?, ?, ?> getPlanningAlgorithm(final UniformCostHTNPlanningProblem planningProblem) {
		return new ForwardDecompositionHTNPlannerBasedOnBestFirst<>(planningProblem, n -> 1.0);
	}
}
