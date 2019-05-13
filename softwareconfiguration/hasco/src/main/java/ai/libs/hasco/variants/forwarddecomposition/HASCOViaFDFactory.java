package ai.libs.hasco.variants.forwarddecomposition;

import ai.libs.hasco.core.DefaultHASCOPlanningGraphGeneratorDeriver;
import ai.libs.hasco.core.HASCOFactory;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.ForwardDecompositionReducer;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public class HASCOViaFDFactory<S extends GraphSearchInput<TFDNode, String>, V extends Comparable<V>> extends HASCOFactory<S, TFDNode, String, V> {

	public HASCOViaFDFactory() {
		super();
		this.setPlanningGraphGeneratorDeriver(new DefaultHASCOPlanningGraphGeneratorDeriver<>(new ForwardDecompositionReducer<>()));
	}

	public HASCOViaFDFactory(final IOptimalPathInORGraphSearchFactory<S, TFDNode, String, V> searchFactory) {
		this();
		this.setSearchFactory(searchFactory);
	}
}
