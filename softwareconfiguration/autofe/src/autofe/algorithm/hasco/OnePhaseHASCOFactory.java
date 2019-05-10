package autofe.algorithm.hasco;

import hasco.core.HASCO;
import hasco.core.HASCOConfig;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstWithRandomCompletionsFactory;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;

public class OnePhaseHASCOFactory extends HASCOViaFDAndBestFirstWithRandomCompletionsFactory {

	private HASCOConfig config;
	private HASCO<GeneralEvaluatedTraversalTree<TFDNode, String, Double>, TFDNode, String, Double> hasco;

	public OnePhaseHASCOFactory(final HASCOConfig config) {
		super();
		this.config = config;
	}

	@Override
	public HASCO<GeneralEvaluatedTraversalTree<TFDNode, String, Double>, TFDNode, String, Double> getAlgorithm() {
		if (this.hasco == null) {
			this.hasco = super.getAlgorithm();
			this.hasco.setConfig(this.config);
		}
		return this.hasco;
	}
}
