package ai.libs.jaicore.ea.algorithm.moea.moeaframework;

import org.moeaframework.problem.AbstractProblem;

import ai.libs.jaicore.ea.algorithm.IEvolutionaryAlgorithmProblem;

public interface IMOEAFrameworkAlgorithmInput extends IEvolutionaryAlgorithmProblem {

	/**
	 * @return Returns a problem instance specific to the MOEAFramework.
	 */
	public AbstractProblem getProblem();

}
