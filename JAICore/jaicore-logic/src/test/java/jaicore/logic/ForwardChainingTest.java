package jaicore.logic;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Map;

import org.junit.Test;

import jaicore.basic.algorithm.AAlgorithmFactory;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.GeneralAlgorithmTester;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.IAlgorithmFactory;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.logic.fol.util.ForwardChainer;
import jaicore.logic.fol.util.ForwardChainingProblem;

public class ForwardChainingTest extends GeneralAlgorithmTester<ForwardChainingProblem, ForwardChainingProblem, Collection<Map<VariableParam, LiteralParam>>>{

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
	public AlgorithmProblemTransformer<ForwardChainingProblem, ForwardChainingProblem> getProblemReducer() {
		return t -> t;
	}

	@Override
	public IAlgorithmFactory<ForwardChainingProblem, Collection<Map<VariableParam, LiteralParam>>> getFactory() {
		return new AAlgorithmFactory<ForwardChainingProblem, Collection<Map<VariableParam, LiteralParam>>>() {

			@Override
			public IAlgorithm<ForwardChainingProblem, Collection<Map<VariableParam, LiteralParam>>> getAlgorithm() {
				ForwardChainer fc = new ForwardChainer();
				fc.setInput(getInput());
				return fc;
			}
		};
	}

	@Override
	public ForwardChainingProblem getSimpleProblemInputForGeneralTestPurposes() throws Exception {

		Monom factbase = new Monom("P('a', 'b') & P('b', 'c') & Q('b', 'c') & Q('c', 'a')");
		Monom conclusion = new Monom("P(x, y) & Q(y, z) & P(y, z) & Q(z, x)");
		
		return new ForwardChainingProblem(factbase, conclusion, false);
	}

	@Override
	public ForwardChainingProblem getDifficultProblemInputForGeneralTestPurposes() throws Exception {
		Monom factBase = getSimpleProblemInputForGeneralTestPurposes().getFactbase();
		Monom conclusion = getSimpleProblemInputForGeneralTestPurposes().getConclusion();
		for (int i = 0; i < 1000; i++) {
			factBase.add(new Literal("P('c" + i + "', 'd" + (i-1) + "')"));
			conclusion.add(new Literal("R('a', 'b" + i + "')"));
		}
		return new ForwardChainingProblem(factBase, conclusion, false);
	}
}
