package jaicore.ea.algorithm.moea.moeaframework;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;

import jaicore.ea.algorithm.IEvolutionaryAlgorithmResult;

public class MOEAFrameworkAlgorithmResult implements IEvolutionaryAlgorithmResult<Population> {

	private final NondominatedPopulation result;
	private final Population population;

	public MOEAFrameworkAlgorithmResult(final NondominatedPopulation result, final Population population) {
		this.result = result;
		this.population = population;
	}

	@Override
	public Population getPopulation() {
		return this.population;
	}

	@Override
	public Population getResult() {
		return this.result;
	}

}
