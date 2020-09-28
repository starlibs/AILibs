package ai.libs.hasco.builder.forwarddecomposition;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.SimpleForwardDecompositionReducer;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness;

public class HASCOViaFDBuilder<V extends Comparable<V>, B extends HASCOViaFDBuilder<V, B>> extends HASCOBuilder<TFDNode, String, V, B> {

	public HASCOViaFDBuilder(final Class<V> scoreClass) {
		super(scoreClass);
		this.withPlanningGraphGeneratorDeriver(new SimpleForwardDecompositionReducer());
	}

	public HASCOViaFDBuilder(final HASCOBuilder<TFDNode, String, V, ?> b) {
		super(b);
		this.withPlanningGraphGeneratorDeriver(new SimpleForwardDecompositionReducer());
	}

	public HASCOViaFDAndDFSBuilder<V, ?> withDFS() {
		return new HASCOViaFDAndDFSBuilder<>(this);
	}

	public HASCOViaFDAndBestFirstBuilder<V, ?> withBestFirst() {
		return new HASCOViaFDAndBestFirstBuilder<>(this);
	}

	public HASCOViaFDAndBestFirstBuilder<Double, ?> withBlindSearch() {
		if (!this.getScoreClass().equals(Double.class)) {
			throw new IllegalStateException("Blind Best First is only possible for node values of type Double, but is " + this.getScoreClass().getName());
		}
		@SuppressWarnings("unchecked")
		HASCOViaFDAndBestFirstBuilder<Double, ?> builder = (HASCOViaFDAndBestFirstBuilder<Double, ?>)this.withBestFirst();
		builder.withReduction(new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness());
		return builder;
	}

	@Override
	public HASCOViaFD<V> getAlgorithm() {
		this.requireThatProblemHasBeenDefined();
		HASCOViaFD<V> hasco = new HASCOViaFD<>(this.getProblem(), this.getSearchFactory());
		hasco.setConfig(this.getHascoConfig());
		return hasco;
	}
}
