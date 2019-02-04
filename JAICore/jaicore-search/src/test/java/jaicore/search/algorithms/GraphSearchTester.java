package jaicore.search.algorithms;

import jaicore.basic.algorithm.SolutionCandidateIteratorTester;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.probleminputs.GraphSearchInput;

public abstract class GraphSearchTester<P, I extends GraphSearchInput<NSrc, ASrc>, O, NSrc, ASrc, NSearch, ASearch> extends SolutionCandidateIteratorTester<P, I, O> {
	
	public abstract IGraphSearchFactory<I, O, NSrc,ASrc, NSearch,ASearch> getFactory();
}
