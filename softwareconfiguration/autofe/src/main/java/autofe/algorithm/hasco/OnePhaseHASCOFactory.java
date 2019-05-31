package autofe.algorithm.hasco;

import hasco.core.HASCOConfig;
import hasco.core.HASCOSolutionCandidate;
import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.optimizingfactory.SoftwareConfigurationAlgorithmFactory;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirst;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstWithRandomCompletionsFactory;
import hasco.variants.forwarddecomposition.twophase.HASCOWithRandomCompletionsConfig;

public class OnePhaseHASCOFactory extends HASCOViaFDAndBestFirstWithRandomCompletionsFactory implements SoftwareConfigurationAlgorithmFactory<RefinementConfiguredSoftwareConfigurationProblem<Double>, HASCOSolutionCandidate<Double>, Double> {

	private HASCOConfig config;
	private HASCOViaFDAndBestFirst<Double> hasco;

	public OnePhaseHASCOFactory(final HASCOWithRandomCompletionsConfig config) {
		super(config.seed(), config.numberOfRandomCompletions());
		this.config = config;
	}

	@Override
	public HASCOViaFDAndBestFirst<Double> getAlgorithm() {
		if (hasco == null) {
			hasco = super.getAlgorithm();
			hasco.setConfig(config);
		}
		return hasco;
	}
}
