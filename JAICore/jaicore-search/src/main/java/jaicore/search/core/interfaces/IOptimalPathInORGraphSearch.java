package jaicore.search.core.interfaces;

import jaicore.basic.algorithm.IOptimizationAlgorithm;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;

/**
 * This is a template for algorithms that aim at finding paths from a root to
 * goal nodes in a graph. This template does not assume paths to have a score.
 * 
 * The output type of this algorithm is fixed to EvaluatedSearchGraphPath<NSrc, ASrc, V>
 * 
 * @author fmohr
 *
 * @param <I>
 * @param <N>
 * @param <A>
 * @param <V>
 * @param <NSearch>
 * @param <Asearch>
 */
public interface IOptimalPathInORGraphSearch<I extends GraphSearchInput<N, A>, N, A, V extends Comparable<V>> extends IOptimizationAlgorithm<I, EvaluatedSearchGraphPath<N, A, V>, V>, IGraphSearch<I, EvaluatedSearchGraphPath<N, A, V>, N, A>, IPathInORGraphSearch<I, EvaluatedSearchGraphPath<N, A, V>, N, A> {
	
}
