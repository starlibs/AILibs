package jaicore.search.core.interfaces;

import java.util.List;

/**
 * This is an extension of the classical GraphGenerator that allows to assert that a path is semantically subsumed by another.
 * This is important if it is not trivially checkable whether two states are identical due to deviating object names of semantically equivalent objects. 
 * 
 * @author fmohr
 *
 * @param <N>
 * @param <A>
 */
public interface PathUnifyingGraphGenerator<N, A> extends GraphGenerator<N, A> {
	public boolean isPathSemanticallySubsumed(List<N> path, List<N> potentialSuperPath) throws InterruptedException;
}
