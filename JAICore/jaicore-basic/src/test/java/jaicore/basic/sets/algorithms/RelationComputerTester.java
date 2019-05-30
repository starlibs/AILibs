package jaicore.basic.sets.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;

import jaicore.basic.algorithm.GeneralAlgorithmTester;
import jaicore.basic.sets.problems.CellPhoneRelationSet;
import jaicore.basic.sets.problems.RelationalProblemSet;

public abstract class RelationComputerTester extends GeneralAlgorithmTester {

	// creates the test data
	@Parameters(name = "problemset = {0}")
	public static Collection<Object[]> data() {
		List<Object> problemSets = new ArrayList<>();

		/* add N-Queens (as a graph search problem set) */
		problemSets.add(new CellPhoneRelationSet());
		List<Collection<Object>> input = new ArrayList<>();
		input.add(problemSets);

		Object[][] data = new Object[problemSets.size()][1];
		for (int i = 0; i < data.length; i++) {
			data[i][0] = problemSets.get(i);
		}
		return Arrays.asList(data);
	}

	@Override
	public RelationalProblemSet getProblemSet() {
		return (RelationalProblemSet)super.getProblemSet();
	}
}
