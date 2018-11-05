package jaicore.search.core.interfaces;

import jaicore.graph.IGraphAlgorithmFactory;

public interface IGraphSearchFactory<I, O, NSrc, ASrc, V extends Comparable<V>, NSearch, ASearch>
		extends IGraphAlgorithmFactory<I, O, NSearch, ASearch> {

	@Override
	public IGraphSearch<I, O, NSrc, ASrc, V, NSearch, ASearch> getAlgorithm();
}
