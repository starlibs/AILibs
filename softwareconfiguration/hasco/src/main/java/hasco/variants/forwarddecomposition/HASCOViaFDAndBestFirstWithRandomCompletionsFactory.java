package hasco.variants.forwarddecomposition;

import java.util.function.Predicate;

import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS;

public class HASCOViaFDAndBestFirstWithRandomCompletionsFactory extends HASCOViaFDAndBestFirstFactory<Double> {

	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator = n -> null;
	private Predicate<TFDNode> priorizingPredicate;
	private int seed;
	private int numSamples;
	private int timeoutForSingleCompletionEvaluationInMS;
	private int timeoutForNodeEvaluationInMS;

	public HASCOViaFDAndBestFirstWithRandomCompletionsFactory() {
		super();
	}
	
	public HASCOViaFDAndBestFirstWithRandomCompletionsFactory(int seed, int numSamples) {
		this(seed, numSamples, -1, -1);
	}

	public HASCOViaFDAndBestFirstWithRandomCompletionsFactory(int seed, int numSamples,
			int timeoutForSingleCompletionEvaluationInMS, int timeoutForNodeEvaluationInMS) {
		super();
		this.seed = seed;
		this.numSamples = numSamples;
		this.timeoutForSingleCompletionEvaluationInMS = timeoutForSingleCompletionEvaluationInMS;
		this.timeoutForNodeEvaluationInMS = timeoutForNodeEvaluationInMS;
	}

	public Predicate<TFDNode> getPriorizingPredicate() {
		return priorizingPredicate;
	}

	public void setPriorizingPredicate(Predicate<TFDNode> priorizingPredicate) {
		this.priorizingPredicate = priorizingPredicate;
	}

	public INodeEvaluator<TFDNode, Double> getPreferredNodeEvaluator() {
		return preferredNodeEvaluator;
	}

	public void setPreferredNodeEvaluator(INodeEvaluator<TFDNode, Double> preferredNodeEvaluator) {
		this.preferredNodeEvaluator = preferredNodeEvaluator;
	}

	@Override
	public HASCOViaFDAndBestFirst<Double> getAlgorithm() {
		setSearchProblemTransformer(
				new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<>(
						preferredNodeEvaluator, priorizingPredicate, seed, numSamples, timeoutForSingleCompletionEvaluationInMS, timeoutForNodeEvaluationInMS));
		return super.getAlgorithm();
	}
}
