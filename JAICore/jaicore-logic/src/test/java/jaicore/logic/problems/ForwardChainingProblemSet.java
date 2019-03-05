package jaicore.logic.problems;

import jaicore.basic.algorithm.IAlgorithmTestProblemSet;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.util.ForwardChainingProblem;

public class ForwardChainingProblemSet extends IAlgorithmTestProblemSet<ForwardChainingProblem, Boolean> {

	public ForwardChainingProblemSet() {
		super("Forward Chaining Problem");
	}

	@Override
	public ForwardChainingProblem getSimpleProblemInputForGeneralTestPurposes() throws Exception {

		Monom factbase = new Monom("P('a', 'b') & P('b', 'c') & Q('b', 'c') & Q('c', 'a')");
		Monom conclusion = new Monom("P(x, y) & Q(y, z) & P(y, z) & Q(z, x)");
		
		return new ForwardChainingProblem(factbase, conclusion, false);
	}

	@Override
	public ForwardChainingProblem getDifficultProblemInputForGeneralTestPurposes() throws Exception {
		return getSimpleProblemInputForGeneralTestPurposes();
	}

}
