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
 * @param <NSrc>
 * @param <ASrc>
 * @param <V>
 * @param <NSearch>
 * @param <Asearch>
 */
public interface IOptimalPathInORGraphSearch<I extends GraphSearchInput<NSrc, ASrc>, NSrc, ASrc, V extends Comparable<V>, NSearch, Asearch> extends IOptimizationAlgorithm<I, EvaluatedSearchGraphPath<NSrc, ASrc, V>, V>, IGraphSearch<I, EvaluatedSearchGraphPath<NSrc, ASrc, V>, NSrc, ASrc, NSearch, Asearch>, IPathInORGraphSearch<I, EvaluatedSearchGraphPath<NSrc, ASrc, V>, NSrc, ASrc, NSearch, Asearch> {
	
}
