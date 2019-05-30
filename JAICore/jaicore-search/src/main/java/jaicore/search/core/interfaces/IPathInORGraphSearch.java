package jaicore.search.core.interfaces;

import jaicore.basic.algorithm.ISolutionCandidateIterator;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;

public interface IPathInORGraphSearch<I extends GraphSearchInput<N, A>, O extends SearchGraphPath<N, A>, N, A> extends IGraphSearch<I, O, N, A>, ISolutionCandidateIterator<I, O> {

}
