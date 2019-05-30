package hasco.variants.forwarddecomposition.twophase;

import hasco.core.HASCO;
import hasco.core.HASCOFactory;
import hasco.core.HASCOSolutionCandidate;
import hasco.optimizingfactory.SoftwareConfigurationAlgorithmFactory;
import jaicore.search.probleminputs.GraphSearchInput;

public class TwoPhaseHASCOFactory<S extends GraphSearchInput<N, A>, N, A> implements SoftwareConfigurationAlgorithmFactory<TwoPhaseSoftwareConfigurationProblem, HASCOSolutionCandidate<Double>, Double> {

	private HASCOFactory<S, N, A, Double> hascoFactory;
	private TwoPhaseSoftwareConfigurationProblem problem;
	private TwoPhaseHASCOConfig config;

	public TwoPhaseHASCOFactory() {
		super();
	}

	public TwoPhaseHASCOFactory(final HASCOFactory<S, N, A, Double> hascoFactory) {
		super();
		this.hascoFactory = hascoFactory;
	}

	public HASCOFactory<S, N, A, Double> getHascoFactory() {
		return this.hascoFactory;
	}

	public void setHascoFactory(final HASCOFactory<S, N, A, Double> hascoFactory) {
		this.hascoFactory = hascoFactory;
	}

	public TwoPhaseHASCOConfig getConfig() {
		return this.config;
	}

	public void setConfig(final TwoPhaseHASCOConfig config) {
		this.config = config;
	}

	@Override
	public TwoPhaseHASCO<S, N, A> getAlgorithm() {
		return this.getAlgorithm(this.problem);
	}

	@Override
	public TwoPhaseHASCO<S, N, A> getAlgorithm(final TwoPhaseSoftwareConfigurationProblem problem) {
		this.hascoFactory.setProblemInput(problem);
		this.hascoFactory.withAlgorithmConfig(this.config);
		HASCO<S, N, A, Double> hasco = this.hascoFactory.getAlgorithm();
		return new TwoPhaseHASCO<>(problem, this.config, hasco);
	}

}
