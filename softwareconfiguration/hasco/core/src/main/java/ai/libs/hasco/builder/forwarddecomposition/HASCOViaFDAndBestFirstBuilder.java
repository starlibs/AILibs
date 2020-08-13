package ai.libs.hasco.builder.forwarddecomposition;

import java.util.Objects;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

/**
 * This factory makes it easier to create HASCO objects. In contrast to the standard HASCOFactory, it is only necessary to set the problem and a node evaluator
 *
 * Note that the standard HASCO search problem is a GraphSearchProblem, but BestFirst needs sub-path evaluation, so providing such a transformation is a MUST.
 *
 * It is possible to set the node evaluator, which will be then used in the search
 *
 * @author Felix Mohr
 *
 * @param <V> The node evaluation type (must be Comparable, typically Double)
 */
public class HASCOViaFDAndBestFirstBuilder<V extends Comparable<V>, B extends HASCOViaFDAndBestFirstBuilder<V, B>> extends HASCOViaFDBuilder<V, B> {

	private AlgorithmicProblemReduction<IPathSearchInput<TFDNode, String>, EvaluatedSearchGraphPath<TFDNode, String, V>, GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, EvaluatedSearchGraphPath<TFDNode, String, V>> reduction;

	public HASCOViaFDAndBestFirstBuilder(final HASCOBuilder<TFDNode, String, V, ?> b) {
		super(b);
		this.withSearchFactory(new BestFirstFactory<>());
	}

	@SuppressWarnings("unchecked")
	@Override
	public BestFirstFactory<IPathSearchWithPathEvaluationsInput<TFDNode, String, V>, TFDNode, String, V> getSearchFactory() {
		return (BestFirstFactory<IPathSearchWithPathEvaluationsInput<TFDNode, String, V>, TFDNode, String, V>) super.getSearchFactory();
	}

	public B withReduction(final AlgorithmicProblemReduction<IPathSearchInput<TFDNode, String>, EvaluatedSearchGraphPath<TFDNode, String, V>, GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, EvaluatedSearchGraphPath<TFDNode, String, V>> reduction) {
		Objects.requireNonNull(reduction);
		this.reduction = reduction;
		return this.getSelf();

	}

	@Override
	public HASCOViaFD<V> getAlgorithm() {
		this.requireThatProblemHasBeenDefined();
		if (this.reduction == null) {
			throw new IllegalStateException("No reduction defined yet.");
		}
		BestFirstFactory<IPathSearchWithPathEvaluationsInput<TFDNode, String, V>, TFDNode, String, V> factory = new BestFirstFactory<>();
		factory.setReduction(this.reduction);
		HASCOViaFD<V> hasco = new HASCOViaFD<>(super.getProblem(), factory);
		hasco.setConfig(this.getHascoConfig());
		return hasco;
	}

	@SuppressWarnings("unchecked")
	public HASCOViaFDAndBestFirstWithRandomCompletionsBuilder withRandomCompletions() {
		if (!this.getScoreClass().equals(Double.class)) {
			throw new IllegalStateException("Random completions only applicable for double-typed problems.");
		}
		return new HASCOViaFDAndBestFirstWithRandomCompletionsBuilder((HASCOViaFDAndBestFirstBuilder<Double, ?>) this);
	}
}
