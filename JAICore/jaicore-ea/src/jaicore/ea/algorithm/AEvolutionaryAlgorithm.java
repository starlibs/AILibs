package jaicore.ea.algorithm;

import jaicore.basic.algorithm.AAlgorithm;

public abstract class AEvolutionaryAlgorithm extends AAlgorithm<IEvolutionaryAlgorithmProblem, IEvolutionaryAlgorithmResult> {

	protected AEvolutionaryAlgorithm(final IEvolutionaryAlgorithmConfig config, final IEvolutionaryAlgorithmProblem problem) {
		super(config, problem);
	}

}
