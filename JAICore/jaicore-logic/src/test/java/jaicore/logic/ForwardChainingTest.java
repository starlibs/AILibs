package jaicore.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import jaicore.basic.algorithm.GeneralAlgorithmTester;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.util.ForwardChainer;
import jaicore.logic.fol.util.ForwardChainingProblem;

public class ForwardChainingTest extends GeneralAlgorithmTester {

	@Test
	public void testSingle() throws Exception {
		Monom factbase = new Monom("P('a', 'b') & P('b', 'c') & Q('b', 'c')");
		Monom conclusion = new Monom("P(x, y) & Q(y, z)");
		
		ForwardChainer fc = new ForwardChainer(new ForwardChainingProblem(factbase, conclusion, false));
		assertEquals(1, fc.call().size());
	}
	
	@Test
	public void testDouble() throws Exception {
		Monom factbase = new Monom("P('a', 'b') & P('b', 'c') & Q('b', 'c') & Q('c', 'a')");
		Monom conclusion = new Monom("P(x, y) & Q(y, z)");
		
		ForwardChainer fc = new ForwardChainer(new ForwardChainingProblem(factbase, conclusion, false));
		assertEquals(2, fc.call().size());
	}

	@Test
	public void testTriple() throws Exception {
		Monom factbase = new Monom("P('a', 'b') & P('b', 'c') & Q('b', 'c') & Q('c', 'a')");
		Monom conclusion = new Monom("P(x, y) & Q(y, z) & P(y, z) & Q(z, x)");
		
		ForwardChainer fc = new ForwardChainer(new ForwardChainingProblem(factbase, conclusion, false));
		assertEquals(1, fc.call().size());
	}
	
	@Test
	public void testCWA() throws Exception {
		Monom factbase = new Monom("P('a', 'b') & P('b', 'c') & Q('b', 'c')");
		Monom conclusion = new Monom("P(x, y) & !Q(y, z)");
		
		ForwardChainer fc = new ForwardChainer(new ForwardChainingProblem(factbase, conclusion, true));
		assertEquals(2, fc.call().size()); // <x,y,z> = <a,b,c>/<b,c,a>
	}
	
	@Override
	public void testInterrupt() throws Exception {
		// can't produce difficult enough problems
	}
	
	@Override
	public void testCancel() throws Exception {
		// can't produce difficult enough problems
	}
	
	@Override
	public void testQuickTimeout() throws Exception {
		// can't produce difficult enough problems
	}

	@Override
	public IAlgorithm<?, ?> getAlgorithm(Object problem) {
		return new ForwardChainer((ForwardChainingProblem)problem);
	}
}
