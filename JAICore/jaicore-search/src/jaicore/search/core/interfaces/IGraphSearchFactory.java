package jaicore.search.core.interfaces;

import jaicore.graph.IGraphAlgorithmFactory;
import jaicore.search.probleminputs.GraphSearchInput;

public interface IGraphSearchFactory<I extends GraphSearchInput<NSrc, ASrc>, O, NSrc, ASrc, NSearch, ASearch>
		extends IGraphAlgorithmFactory<I, O, NSearch, ASearch> {

	@Override
	public IGraphSearch<I, O, NSrc, ASrc, NSearch, ASearch> getAlgorithm();
}
