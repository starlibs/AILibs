package ai.libs.jaicore.search.core.interfaces;

import ai.libs.jaicore.basic.algorithm.ISolutionCandidateIterator;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public interface IPathInORGraphSearch<I extends GraphSearchInput<N, A>, O extends SearchGraphPath<N, A>, N, A> extends IGraphSearch<I, O, N, A>, ISolutionCandidateIterator<I, O> {

}
