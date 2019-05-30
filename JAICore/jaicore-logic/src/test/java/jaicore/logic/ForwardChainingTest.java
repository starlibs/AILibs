package jaicore.logic;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.GeneralAlgorithmTester;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.util.ForwardChainer;
import jaicore.logic.fol.util.ForwardChainingProblem;
import jaicore.logic.problems.ForwardChainingProblemSet;

public class ForwardChainingTest extends GeneralAlgorithmTester {

	@Parameters(name = "problemset = {0}")
	public static Collection<Object[]> data() {
		List<Object> problemSets = new ArrayList<>();

		/* add N-Queens (as a graph search problem set) */
		problemSets.add(new ForwardChainingProblemSet());
		List<Collection<Object>> input = new ArrayList<>();
		input.add(problemSets);

		Object[][] data = new Object[problemSets.size()][1];
		for (int i = 0; i < data.length; i++) {
			data[i][0] = problemSets.get(i);
		}
		return Arrays.asList(data);
	}

	@Test
	public void testSingle() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {
		Monom factbase = new Monom("P('a', 'b') & P('b', 'c') & Q('b', 'c')");
		Monom conclusion = new Monom("P(x, y) & Q(y, z)");

		ForwardChainer fc = new ForwardChainer(new ForwardChainingProblem(factbase, conclusion, false));
		assertEquals(1, fc.call().size());
	}

	@Test
	public void testDouble() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException  {
		Monom factbase = new Monom("P('a', 'b') & P('b', 'c') & Q('b', 'c') & Q('c', 'a')");
		Monom conclusion = new Monom("P(x, y) & Q(y, z)");

		ForwardChainer fc = new ForwardChainer(new ForwardChainingProblem(factbase, conclusion, false));
		assertEquals(2, fc.call().size());
	}

	@Test
	public void testTriple() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException  {
		Monom factbase = new Monom("P('a', 'b') & P('b', 'c') & Q('b', 'c') & Q('c', 'a')");
		Monom conclusion = new Monom("P(x, y) & Q(y, z) & P(y, z) & Q(z, x)");

		ForwardChainer fc = new ForwardChainer(new ForwardChainingProblem(factbase, conclusion, false));
		assertEquals(1, fc.call().size());
	}

	@Test
	public void testCWA() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {
		Monom factbase = new Monom("P('a', 'b') & P('b', 'c') & Q('b', 'c')");
		Monom conclusion = new Monom("P(x, y) & !Q(y, z)");

		ForwardChainer fc = new ForwardChainer(new ForwardChainingProblem(factbase, conclusion, true));
		assertEquals(2, fc.call().size()); // <x,y,z> = <a,b,c>/<b,c,a>
	}

	@Override
	public void testInterrupt() {
		// can't produce difficult enough problems
	}

	@Override
	public void testCancel() {
		// can't produce difficult enough problems
	}

	@Override
	public void testTimeout() {
		// can't produce difficult enough problems
	}

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final Object problem) {
		return new ForwardChainer((ForwardChainingProblem)problem);
	}
}
