package ai.libs.hasco.builder.forwarddecomposition;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.SimpleForwardDecompositionReducer;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;

public class HASCOViaFDBuilder<V extends Comparable<V>, B extends HASCOViaFDBuilder<V, B>> extends HASCOBuilder<TFDNode, String, V, B> {

	public HASCOViaFDBuilder(final Class<V> scoreClass) {
		super(scoreClass);
		this.withPlanningGraphGeneratorDeriver(new SimpleForwardDecompositionReducer());
	}

	public HASCOViaFDBuilder(final HASCOBuilder<TFDNode, String, V, ?> b) {
		super(b);
		this.withPlanningGraphGeneratorDeriver(new SimpleForwardDecompositionReducer());
	}

	public HASCOViaFDAndBestFirstBuilder<V, ?> withBestFirst() {
		return new HASCOViaFDAndBestFirstBuilder<>(this);
	}

	public HASCOViaFDAndBestFirstBuilder<Double, ?> withBlindSearch() {
		if (!this.getScoreClass().equals(Double.class)) {
			throw new IllegalStateException("Blind Best First is only possible for node values of type Double, but is " + this.getScoreClass().getName());
		}
		HASCOViaFDAndBestFirstBuilder<Double, ?> builder = (HASCOViaFDAndBestFirstBuilder<Double, ?>)this.withBestFirst();
		builder.withNodeEvaluator(n -> 0.0);
		return builder;
	}
}
