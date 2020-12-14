package ai.libs.jaicore.planning.tests;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.IOptimizationAlgorithm;
import org.junit.jupiter.params.provider.Arguments;

import ai.libs.jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import ai.libs.jaicore.basic.algorithm.GeneralAlgorithmTester;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.UniformCostHTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.testproblems.dockworker.STNDockworkerProblemSet;
import ai.libs.jaicore.planning.hierarchical.testproblems.nesteddichotomies.CEOCSTNNestedDichotomyProblemSet;

public abstract class OptimizingHTNPlanningAlgorithmTester extends GeneralAlgorithmTester {

	public static Stream<Arguments> getProblemSets() {
		List<AAlgorithmTestProblemSet<?>> problemSets = new ArrayList<>();
		problemSets.add(new STNDockworkerProblemSet());
		problemSets.add(new CEOCSTNNestedDichotomyProblemSet());
		return problemSets.stream().map(Arguments::of);
	}

	@Override
	public final IAlgorithm<?, ?> getAlgorithm(final Object problem) {
		return this.getPlanningAlgorithm(new UniformCostHTNPlanningProblem((IHTNPlanningProblem) problem));
	}

	public abstract IOptimizationAlgorithm<?, ?, ?> getPlanningAlgorithm(UniformCostHTNPlanningProblem planningProblem);
}
