package autofe.algorithm.hasco;

import ai.libs.hasco.core.HASCOConfig;
import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.hasco.optimizingfactory.SoftwareConfigurationAlgorithmFactory;
import ai.libs.hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirst;
import ai.libs.hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstWithRandomCompletionsFactory;
import ai.libs.hasco.variants.forwarddecomposition.twophase.HASCOWithRandomCompletionsConfig;

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
