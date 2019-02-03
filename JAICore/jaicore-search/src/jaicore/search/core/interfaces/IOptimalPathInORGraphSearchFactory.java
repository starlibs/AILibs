package jaicore.search.core.interfaces;

import jaicore.graph.IGraphAlgorithmFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;

public interface IOptimalPathInORGraphSearchFactory<I extends GraphSearchInput<NSrc, ASrc>,NSrc, ASrc, V extends Comparable<V>, NSearch, ASearch>
		extends IGraphAlgorithmFactory<I, EvaluatedSearchGraphPath<NSrc, ASrc, V>, NSearch, ASearch> {

	@Override
	public IOptimalPathInORGraphSearch<I, NSrc, ASrc, V, NSearch, ASearch> getAlgorithm();
}
