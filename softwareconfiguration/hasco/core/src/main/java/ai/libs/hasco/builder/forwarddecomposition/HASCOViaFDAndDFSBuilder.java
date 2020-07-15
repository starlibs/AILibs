package ai.libs.hasco.builder.forwarddecomposition;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.auxilliary.iteratingoptimizer.IteratingGraphSearchOptimizerFactory;
import ai.libs.jaicore.search.algorithms.standard.dfs.DepthFirstSearchFactory;

public class HASCOViaFDAndDFSBuilder<V extends Comparable<V>, B extends HASCOViaFDAndDFSBuilder<V, B>> extends HASCOViaFDBuilder<V, B> {

	public HASCOViaFDAndDFSBuilder(final Class<V> scoreClass) {
		super(scoreClass);
	}

	public HASCOViaFDAndDFSBuilder(final HASCOBuilder<TFDNode, String, V, ?> b) {
		super(b);
		this.withSearchFactory(new IteratingGraphSearchOptimizerFactory<>(new DepthFirstSearchFactory<>()));
	}
}
