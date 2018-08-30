package jaicore.search.core.interfaces;

import jaicore.graph.IGraphAlgorithmListener;
import jaicore.graph.StandardGraphAlgorithmFactory;

public abstract class StandardORGraphSearchFactory<I, O, NSrc, ASrc, V extends Comparable<V>, NSearch, ASearch, L extends IGraphAlgorithmListener<NSearch, ASearch>> extends StandardGraphAlgorithmFactory<I, O, NSearch, ASearch, L> implements IGraphSearchFactory<I, O, NSrc, ASrc, V, NSearch, ASearch, L> {

}
