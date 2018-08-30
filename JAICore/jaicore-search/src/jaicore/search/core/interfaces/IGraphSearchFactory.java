package jaicore.search.core.interfaces;

import jaicore.graph.IGraphAlgorithmFactory;
import jaicore.graph.IGraphAlgorithmListener;

public interface IGraphSearchFactory<I, O, NSrc, ASrc, V extends Comparable<V>, NSearch, ASearch, L extends IGraphAlgorithmListener<NSearch, ASearch>>
		extends IGraphAlgorithmFactory<I, O, NSearch, ASearch, L> {

	@Override
	public IGraphSearch<I, O, NSrc, ASrc, V, NSearch, ASearch, L> getAlgorithm();
}
