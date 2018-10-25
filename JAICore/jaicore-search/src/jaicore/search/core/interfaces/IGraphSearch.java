package jaicore.search.core.interfaces;

import java.util.NoSuchElementException;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.graph.IGraphAlgorithm;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.other.SearchGraphPath;

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
 * @param <L>
 */
public interface IGraphSearch<I, O, NSrc, ASrc, V extends Comparable<V>, NSearch, ASearch> extends IGraphAlgorithm<I, O, NSearch, ASearch> {
	public GraphGenerator<NSrc, ASrc> getGraphGenerator();

	public <U extends SearchGraphPath<NSrc, ASrc>> U nextSolution() throws InterruptedException, AlgorithmExecutionCanceledException, NoSuchElementException;

	public EvaluatedSearchGraphPath<NSrc, ASrc, V> getBestSeenSolution();
}
