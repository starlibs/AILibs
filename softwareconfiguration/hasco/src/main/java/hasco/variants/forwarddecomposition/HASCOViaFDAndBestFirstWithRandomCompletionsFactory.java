package hasco.variants.forwarddecomposition;

import java.util.function.Predicate;

import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.StandardBestFirstFactory;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS;

public class HASCOViaFDAndBestFirstWithRandomCompletionsFactory extends HASCOViaFDAndBestFirstFactory<Double> {

	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator = n -> null;
	private Predicate<TFDNode> priorizingPredicate;
	private int seed;
	private int numSamples;
	private int timeoutForSingleCompletionEvaluationInMS;
	private int timeoutForNodeEvaluationInMS;

	public HASCOViaFDAndBestFirstWithRandomCompletionsFactory(final int seed, final int numSamples) {
		this(seed, numSamples, -1, -1);
	}

	public HASCOViaFDAndBestFirstWithRandomCompletionsFactory(final int seed, final int numSamples, final int timeoutForSingleCompletionEvaluationInMS, final int timeoutForNodeEvaluationInMS) {
		super();
		this.seed = seed;
		this.numSamples = numSamples;
		this.timeoutForSingleCompletionEvaluationInMS = timeoutForSingleCompletionEvaluationInMS;
		this.timeoutForNodeEvaluationInMS = timeoutForNodeEvaluationInMS;
	}

	public Predicate<TFDNode> getPriorizingPredicate() {
		return this.priorizingPredicate;
	}

	public void setPriorizingPredicate(final Predicate<TFDNode> priorizingPredicate) {
		this.priorizingPredicate = priorizingPredicate;
	}

	public INodeEvaluator<TFDNode, Double> getPreferredNodeEvaluator() {
		return this.preferredNodeEvaluator;
	}

	public void setPreferredNodeEvaluator(final INodeEvaluator<TFDNode, Double> preferredNodeEvaluator) {
		this.preferredNodeEvaluator = preferredNodeEvaluator;
	}

	@Override
	public HASCOViaFDAndBestFirst<Double> getAlgorithm() {
		return this.getAlgorithm(this.getProblem());
	}

	@Override
	public HASCOViaFDAndBestFirst<Double> getAlgorithm(final RefinementConfiguredSoftwareConfigurationProblem<Double> problem) {
		this.setSearchProblemTransformer(new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<>(this.preferredNodeEvaluator, this.priorizingPredicate, this.seed, this.numSamples,
				this.timeoutForSingleCompletionEvaluationInMS, this.timeoutForNodeEvaluationInMS));
		this.setSearchFactory(new StandardBestFirstFactory<>());
		return new HASCOViaFDAndBestFirst<>(super.getAlgorithm(problem));
	}
}
