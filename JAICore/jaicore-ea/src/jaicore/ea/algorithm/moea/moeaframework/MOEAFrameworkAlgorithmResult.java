package jaicore.ea.algorithm.moea.moeaframework;

import org.moeaframework.core.NondominatedPopulation;

import jaicore.ea.algorithm.IEvolutionaryAlgorithmResult;

public class MOEAFrameworkAlgorithmResult implements IEvolutionaryAlgorithmResult<NondominatedPopulation> {

	private final NondominatedPopulation result;

	public MOEAFrameworkAlgorithmResult(final NondominatedPopulation result) {
		this.result = result;
	}

	@Override
	public NondominatedPopulation getPopulation() {
		return null;
	}

	@Override
	public NondominatedPopulation getResult() {
		return this.result;
	}

}
