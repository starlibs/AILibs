package jaicore.search.core.interfaces;

import jaicore.graph.IGraphAlgorithm;
import jaicore.search.probleminputs.GraphSearchInput;

/**
 * Graph search algorithms take a graph <VSrc, ESrc> that is given in the form of a graph generator and search it. Usually, the algorithm uses internal wrapper classes to represent edges and nodes, which is why there are additional generics for that.
 * 
 * @author fmohr
 * 
 * @param <I>
 * @param <O>
 * @param <NSrc>
 * @param <ASrc>
 * @param <NSearch>
 * @param <ASearch>
 */
public interface IGraphSearch<I extends GraphSearchInput<NSrc, ASrc>, O, NSrc, ASrc, NSearch, ASearch> extends IGraphAlgorithm<I, O, NSearch, ASearch> {
	public GraphGenerator<NSrc, ASrc> getGraphGenerator();
}
