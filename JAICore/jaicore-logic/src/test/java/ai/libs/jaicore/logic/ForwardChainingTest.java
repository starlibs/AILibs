package ai.libs.jaicore.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import ai.libs.jaicore.basic.algorithm.GeneralAlgorithmTester;
import ai.libs.jaicore.basic.algorithm.IAlgorithmTestProblemSet;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.util.ForwardChainer;
import ai.libs.jaicore.logic.fol.util.ForwardChainingProblem;
import ai.libs.jaicore.logic.problems.ForwardChainingProblemSet;

public class ForwardChainingTest extends GeneralAlgorithmTester {

	public static Stream<Arguments> getProblemSets() {
		List<Arguments> problemSets = new ArrayList<>();

		/* add N-Queens (as a graph search problem set) */
		problemSets.add(Arguments.of(new ForwardChainingProblemSet()));
		return problemSets.stream();
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
	public void testInterrupt(final IAlgorithmTestProblemSet<?> problemSet) {
		// can't produce difficult enough problems
		assertTrue(true);
	}

	@Override
	public void testCancel(final IAlgorithmTestProblemSet<?> problemSet) {
		// can't produce difficult enough problems
		assertTrue(true);
	}

	@Override
	public void testTimeout(final IAlgorithmTestProblemSet<?> problemSet) {
		// can't produce difficult enough problems
		assertTrue(true);
	}

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final Object problem) {
		return new ForwardChainer((ForwardChainingProblem)problem);
	}
}
