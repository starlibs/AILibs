package ai.libs.hasco.builder.forwarddecomposition;

import java.util.Objects;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer;

/**
 * This factory makes it easier to create HASCO objects. In contrast to the standard HASCOFactory, it is only necessary to set the problem and a node evaluator
 *
 * Note that the standard HASCO search problem is a GraphSearchProblem, but BestFirst needs sub-path evaluation, so providing such a transformation is a MUST.
 *
 * It is possible to set the node evaluator, which will be then used in the search
 *
 * @author Felix Mohr
 *
 * @param <V>
 */
public class HASCOViaFDAndBestFirstBuilder<V extends Comparable<V>, B extends HASCOViaFDAndBestFirstBuilder<V, B>> extends HASCOViaFDBuilder<V, B> {

	private IPathEvaluator<TFDNode, String, V> nodeEvaluator;

	public HASCOViaFDAndBestFirstBuilder(final HASCOBuilder<TFDNode, String, V, ?> b) {
		super(b);
		this.withSearchFactory(new BestFirstFactory<>());
	}

	@SuppressWarnings("unchecked")
	@Override
	public BestFirstFactory<IPathSearchWithPathEvaluationsInput<TFDNode, String, V>, TFDNode, String, V> getSearchFactory() {
		return (BestFirstFactory<IPathSearchWithPathEvaluationsInput<TFDNode, String, V>, TFDNode, String, V>) super.getSearchFactory();
	}

	public B withNodeEvaluator(final IPathEvaluator<TFDNode, String, V> nodeEvaluator) {
		Objects.requireNonNull(nodeEvaluator);
		this.nodeEvaluator = nodeEvaluator;
		return this.getSelf();

	}

	public IPathEvaluator<TFDNode, String, V> getNodeEvaluator() {
		return this.nodeEvaluator;
	}

	@Override
	public HASCOViaFD<V> getAlgorithm() {
		this.requireThatProblemHasBeenDefined();
		if (this.nodeEvaluator == null) {
			throw new IllegalStateException("No node evaluator defined yet.");
		}
		BestFirstFactory<IPathSearchWithPathEvaluationsInput<TFDNode, String, V>, TFDNode, String, V> factory = new BestFirstFactory<>();
		factory.setReduction(new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<>(this.nodeEvaluator));
		HASCOViaFD<V> hasco = new HASCOViaFD<>(super.getProblem(), factory);
		hasco.setConfig(this.getHascoConfig());
		return hasco;
	}

	@SuppressWarnings("unchecked")
	public HASCOViaFDAndBestFirstWithRandomCompletionsBuilder viaRandomCompletions() {
		if (!this.getScoreClass().equals(Double.class)) {
			throw new IllegalStateException("Random completions only applicable for double-typed problems.");
		}
		return new HASCOViaFDAndBestFirstWithRandomCompletionsBuilder((HASCOViaFDAndBestFirstBuilder<Double, ?>) this);
	}
}
