package jaicore.basic.algorithm;

import jaicore.basic.ScoredItem;

/**
 * Optimizers usually do not compute their output ad-hoc but, in many iterations, update the currently best known solution.
 * Hence, optimizers are iterators for solution candidates, and we demand the possibility to ask for next such candidates.
 * 
 * @author fmohr
 *
 * @param <I>
 * @param <O>
 * @param <V>
 */
public interface IOptimizationAlgorithm<I, O extends ScoredItem<V>, V extends Comparable<V>> extends IAlgorithm<I, O>, ISolutionCandidateIterator<I, O> {
	
}
