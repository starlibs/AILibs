package ai.libs.jaicore.ea.algorithm.moea.moeaframework;

import org.moeaframework.problem.AbstractProblem;
import org.moeaframework.problem.CEC2009.CF5;

import ai.libs.jaicore.ea.algorithm.moea.moeaframework.IMOEAFrameworkAlgorithmInput;

public class SimpleTestProblem implements IMOEAFrameworkAlgorithmInput {

	private final int numberOfVariables;

	public SimpleTestProblem() {
		this.numberOfVariables = 10;
	}

	public SimpleTestProblem(final int numberOfVariables) {
		this.numberOfVariables = numberOfVariables;
	}

	@Override
	public AbstractProblem getProblem() {
		return new CF5(this.numberOfVariables);
	}

}
