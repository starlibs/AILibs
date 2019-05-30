package hasco.variants.forwarddecomposition;

import hasco.core.DefaultHASCOPlanningGraphGeneratorDeriver;
import hasco.core.HASCOFactory;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.ForwardDecompositionReducer;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import jaicore.search.probleminputs.GraphSearchInput;

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
