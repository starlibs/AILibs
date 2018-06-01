package hasco.core;

import jaicore.planning.algorithms.IPathToPlanConverter;
import jaicore.search.algorithms.interfaces.IPathUnification;

/**
 * Abstract factory to create all the utility objects necessary to conduct the search with HASCO
 * 
 * @author fmohr
 *
 * @param <N>
 * @param <A>
 * @param <V>
 */
public interface IHASCOSearchSpaceUtilFactory<N,A,V extends Comparable<V>> {
	public IPathUnification<N> getPathUnifier();
	
	public IPathToPlanConverter<N> getPathToPlanConverter();
}
