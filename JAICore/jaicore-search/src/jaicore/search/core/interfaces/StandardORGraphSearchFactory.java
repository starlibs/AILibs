package jaicore.search.core.interfaces;

import jaicore.graph.StandardGraphAlgorithmFactory;
import jaicore.search.probleminputs.GraphSearchInput;

public abstract class StandardORGraphSearchFactory<I extends GraphSearchInput<NSrc, ASrc>, O, NSrc, ASrc, V extends Comparable<V>, NSearch, ASearch> extends StandardGraphAlgorithmFactory<I, O, NSearch, ASearch> implements IGraphSearchFactory<I, O, NSrc, ASrc, NSearch, ASearch> {

}
