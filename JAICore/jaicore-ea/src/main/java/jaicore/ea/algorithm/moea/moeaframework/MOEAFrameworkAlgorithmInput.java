package jaicore.ea.algorithm.moea.moeaframework;

import org.moeaframework.problem.AbstractProblem;

public class MOEAFrameworkAlgorithmInput implements IMOEAFrameworkAlgorithmInput {

	private AbstractProblem problem;

	public MOEAFrameworkAlgorithmInput(final AbstractProblem problem) {
		this.problem = problem;
	}

	@Override
	public AbstractProblem getProblem() {
		return this.problem;
	}

}
