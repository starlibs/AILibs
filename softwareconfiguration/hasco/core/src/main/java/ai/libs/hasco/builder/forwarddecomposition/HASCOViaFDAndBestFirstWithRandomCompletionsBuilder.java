package ai.libs.hasco.builder.forwarddecomposition;

import java.util.Random;
import java.util.function.Predicate;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.algorithm.Timeout;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.core.HASCOUtil;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;

public class HASCOViaFDAndBestFirstWithRandomCompletionsBuilder extends HASCOViaFDAndBestFirstBuilder<Double, HASCOViaFDAndBestFirstWithRandomCompletionsBuilder> {

	private IPathEvaluator<TFDNode, String, Double> preferredNodeEvaluator = n -> null;
	private Predicate<TFDNode> priorizingPredicate;
	private Random random = new Random();
	private int numSamples = 10;
	private int timeoutForSingleCompletionEvaluationInMS = -1;
	private int timeoutForNodeEvaluationInMS = -1;

	public HASCOViaFDAndBestFirstWithRandomCompletionsBuilder(final HASCOBuilder<TFDNode, String, Double, ?> builder) {
		super(builder);
	}

	public Predicate<TFDNode> getPriorizingPredicate() {
		return this.priorizingPredicate;
	}

	public HASCOViaFDAndBestFirstWithRandomCompletionsBuilder withPriorizingPredicate(final Predicate<TFDNode> priorizingPredicate) {
		this.priorizingPredicate = priorizingPredicate;
		return this.getSelf();
	}

	public HASCOViaFDAndBestFirstWithRandomCompletionsBuilder withRandom(final Random random) {
		this.random = random;
		return this.getSelf();
	}

	public HASCOViaFDAndBestFirstWithRandomCompletionsBuilder withSeed(final long seed) {
		return this.withRandom(new Random(seed));
	}

	public HASCOViaFDAndBestFirstWithRandomCompletionsBuilder withNumSamples(final int samples) {
		this.numSamples = samples;
		return this.getSelf();
	}

	public HASCOViaFDAndBestFirstWithRandomCompletionsBuilder withTimeoutForNode(final Timeout to) {
		this.timeoutForNodeEvaluationInMS = (int)to.milliseconds();
		return this.getSelf();
	}

	public HASCOViaFDAndBestFirstWithRandomCompletionsBuilder withTimeoutForSingleEvaluation(final Timeout to) {
		this.timeoutForSingleCompletionEvaluationInMS = (int)to.milliseconds();
		return this.getSelf();
	}

	public IPathEvaluator<TFDNode, String,Double> getPreferredNodeEvaluator() {
		return this.preferredNodeEvaluator;
	}

	public HASCOViaFDAndBestFirstWithRandomCompletionsBuilder withPreferredNodeEvaluator(final IPathEvaluator<TFDNode, String, Double> preferredNodeEvaluator) {
		this.preferredNodeEvaluator = preferredNodeEvaluator;
		return this.getSelf();
	}

	@Override
	public HASCOViaFD<Double> getAlgorithm(){

		/* create node evaluator */
		this.requireThatProblemHasBeenDefined();
		IPathEvaluator<TFDNode, String, Double> pathEvaluator = HASCOUtil.getSearchProblemWithEvaluation(this.getProblem(), this.getPlanningGraphGeneratorDeriver()).getPathEvaluator();
		IPathEvaluator<TFDNode, String, Double> nodeEvaluator = new RandomCompletionBasedNodeEvaluator<>(this.random, this.numSamples, this.numSamples * 2, pathEvaluator,
				this.timeoutForSingleCompletionEvaluationInMS, this.timeoutForNodeEvaluationInMS, n -> false);
		this.withNodeEvaluator(nodeEvaluator);

		/* now get algorithm */
		HASCOViaFD<Double> hasco = super.getAlgorithm();
		((RandomCompletionBasedNodeEvaluator<TFDNode, String, Double>)hasco.getSearch().getInput().getPathEvaluator()).getSolutionEvaluator();
	}
}
