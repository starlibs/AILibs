package jaicore.search.algorithms.standard.rstar;


import java.util.Collection;
import java.util.HashSet;

/**
 * Successor generator class for R*'s Gamma graph.
 * It must ensure, that a state that is generated twice is always
 * belongs to the same instance, i.e. has always the same reference.
 *
 * @param <T> state type
 * @param <D> delta distance type
 */
public abstract class GammaSuccessorGenerator<T, V extends Comparable<V>, D> {

    // TODO: What implementation of set fits best here?
    private HashSet<GammaNode<T, V>> alreadyGeneratedStates = new HashSet<>();

    protected abstract Collection<T> _generateSuccessors(T state, int K, D delta);

    public Collection<T> generateSuccessors(T state, int K, D delta) {
        return null;
    }


}
