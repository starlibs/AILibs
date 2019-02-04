package jaicore.search.core.interfaces;

import jaicore.basic.algorithm.ISolutionCandidateIterator;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;

public interface IPathInORGraphSearch<I extends GraphSearchInput<NSrc, ASrc>, O extends SearchGraphPath<NSrc, ASrc>, NSrc, ASrc, NSearch, ASearch> extends IGraphSearch<I, O, NSrc, ASrc, NSearch, ASearch>, ISolutionCandidateIterator<O> {

}
