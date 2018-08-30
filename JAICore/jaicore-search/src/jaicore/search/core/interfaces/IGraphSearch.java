package jaicore.search.core.interfaces;

import java.util.NoSuchElementException;

import jaicore.graph.IGraphAlgorithm;
import jaicore.graph.IGraphAlgorithmListener;
import jaicore.search.model.other.EvaluatedSearchGraphPath;

/**
 * Graph search algorithms take a graph <VSrc, ESrc> that is given in the form of a graph generator and search it. Usually, the algorithm uses internal wrapper classes to represent edges and nodes, which is why there are additional generics
 * for that.
 * 
 * @author fmohr
 * 
 * @param <I>
 * @param <O>
 * @param <NSrc>
 * @param <ASrc>
 * @param <NSearch>
 * @param <ASearch>
 * @param <L>
 */
public interface IGraphSearch<I, O, NSrc, ASrc, V extends Comparable<V>, NSearch, ASearch, L extends IGraphAlgorithmListener<NSearch, ASearch>> extends IGraphAlgorithm<I, O, NSearch, ASearch, L> {
	public GraphGenerator<NSrc,ASrc> getGraphGenerator();
	public EvaluatedSearchGraphPath<NSrc, ASrc, V> nextSolution() throws InterruptedException, NoSuchElementException;
}
