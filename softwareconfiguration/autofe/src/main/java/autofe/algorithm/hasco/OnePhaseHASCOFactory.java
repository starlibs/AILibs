package autofe.algorithm.hasco;

import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.HASCOConfig;
import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.hasco.optimizingfactory.SoftwareConfigurationAlgorithmFactory;
import ai.libs.hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirst;
import ai.libs.hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstWithRandomCompletionsFactory;
import ai.libs.hasco.variants.forwarddecomposition.twophase.HASCOWithRandomCompletionsConfig;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class OnePhaseHASCOFactory extends HASCOViaFDAndBestFirstWithRandomCompletionsFactory implements SoftwareConfigurationAlgorithmFactory<RefinementConfiguredSoftwareConfigurationProblem<Double>, HASCOSolutionCandidate<Double>, Double, HASCO<GraphSearchWithSubpathEvaluationsInput<TFDNode,String,Double>,TFDNode,String,Double>> {

	private HASCOConfig config;
	private HASCOViaFDAndBestFirst<Double> hasco;

	public OnePhaseHASCOFactory(final HASCOWithRandomCompletionsConfig config) {
		super(config.seed(), config.numberOfRandomCompletions());
		this.config = config;
	}

	@Override
	public HASCOViaFDAndBestFirst<Double> getAlgorithm() {
		if (this.hasco == null) {
			this.hasco = super.getAlgorithm();
			this.hasco.setConfig(this.config);
		}
		return this.hasco;
	}
}
