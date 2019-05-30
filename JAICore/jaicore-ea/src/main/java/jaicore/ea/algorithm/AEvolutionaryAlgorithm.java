package jaicore.ea.algorithm;

import jaicore.basic.algorithm.AAlgorithm;

public abstract class AEvolutionaryAlgorithm<P> extends AAlgorithm<IEvolutionaryAlgorithmProblem, IEvolutionaryAlgorithmResult<P>> {

	protected AEvolutionaryAlgorithm(final IEvolutionaryAlgorithmConfig config, final IEvolutionaryAlgorithmProblem problem) {
		super(config, problem);
	}

}
