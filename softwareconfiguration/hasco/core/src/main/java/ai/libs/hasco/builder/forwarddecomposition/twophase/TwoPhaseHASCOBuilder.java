package ai.libs.hasco.builder.forwarddecomposition.twophase;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.jaicore.components.optimizingfactory.SoftwareConfigurationAlgorithmFactory;

public class TwoPhaseHASCOBuilder<N, A> implements SoftwareConfigurationAlgorithmFactory<TwoPhaseSoftwareConfigurationProblem, HASCOSolutionCandidate<Double>, Double, TwoPhaseHASCO<N, A>> {

	private HASCOBuilder<N, A, Double, ?> hascoFactory;
	private TwoPhaseSoftwareConfigurationProblem problem;
	private TwoPhaseHASCOConfig config;

	public TwoPhaseHASCOBuilder() {
		super();
	}

	public TwoPhaseHASCOBuilder(final HASCOBuilder<N, A, Double, ?> hascoFactory) {
		super();
		this.hascoFactory = hascoFactory;
	}

	public HASCOBuilder<N, A, Double, ?> getHascoFactory() {
		return this.hascoFactory;
	}

	public void setHascoFactory(final HASCOBuilder<N, A, Double, ?> hascoFactory) {
		this.hascoFactory = hascoFactory;
	}

	public TwoPhaseHASCOConfig getConfig() {
		return this.config;
	}

	public void setConfig(final TwoPhaseHASCOConfig config) {
		this.config = config;
	}

	@Override
	public TwoPhaseHASCO<N, A> getAlgorithm() {
		return this.getAlgorithm(this.problem);
	}

	@Override
	public TwoPhaseHASCO<N, A> getAlgorithm(final TwoPhaseSoftwareConfigurationProblem problem) {
		this.hascoFactory.setProblemInput(problem);
		this.hascoFactory.withAlgorithmConfig(this.config);
		HASCO<N, A, Double> hasco = this.hascoFactory.getAlgorithm();
		return new TwoPhaseHASCO<>(problem, this.config, hasco);
	}

}
