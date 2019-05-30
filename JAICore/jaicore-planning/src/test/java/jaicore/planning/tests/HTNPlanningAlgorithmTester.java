package jaicore.planning.tests;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;

import jaicore.basic.algorithm.GeneralAlgorithmTester;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import jaicore.planning.hierarchical.testproblems.dockworker.STNDockworkerProblemSet;
import jaicore.planning.hierarchical.testproblems.nesteddichotomies.CEOCSTNNestedDichotomyProblemSet;

public abstract class HTNPlanningAlgorithmTester extends GeneralAlgorithmTester {

	// creates the test data
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		List<Object> problemSets = new ArrayList<>();
		problemSets.add(new STNDockworkerProblemSet());
		problemSets.add(new CEOCSTNNestedDichotomyProblemSet());
		Object[][] data = new Object[problemSets.size()][1];
		for (int i = 0; i < data.length; i++) {
			data[i][0] = problemSets.get(i);
		}
		return Arrays.asList(data);
	}

	@Override
	public final IAlgorithm<?, ?> getAlgorithm(final Object problem) {
		return this.getPlanningAlgorithm((IHTNPlanningProblem) problem);
	}

	public abstract IAlgorithm<?, ?> getPlanningAlgorithm(IHTNPlanningProblem planningProblem);
}
