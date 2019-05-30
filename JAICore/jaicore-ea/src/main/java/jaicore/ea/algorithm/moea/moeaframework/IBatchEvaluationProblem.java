package jaicore.ea.algorithm.moea.moeaframework;

import java.util.List;

import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

public interface IBatchEvaluationProblem extends Problem {

	/**
	 * Evaluates a batch of individuals.
	 * @param batch The batch of individuals to be evaluated.
	 */
	public void evaluateBatch(List<Solution> batch);

}
