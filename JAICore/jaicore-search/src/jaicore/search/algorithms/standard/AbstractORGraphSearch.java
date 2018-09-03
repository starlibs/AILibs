package jaicore.search.algorithms.standard;

import java.util.NoSuchElementException;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.GraphSearchInput;

public abstract class AbstractORGraphSearch<I extends GraphSearchInput<NSrc, ASrc>, O, NSrc, ASrc, V extends Comparable<V>, NSearch, Asearch>
		implements IGraphSearch<I, O, NSrc, ASrc, V, NSearch, Asearch> {

	protected final I problem;

	public AbstractORGraphSearch(I problem) {
		super();
		this.problem = problem;
	}

	@SuppressWarnings("unchecked")
	public EvaluatedSearchGraphPath<NSrc, ASrc, V> nextSolution() throws InterruptedException, AlgorithmExecutionCanceledException, NoSuchElementException {
		for (AlgorithmEvent event : this) {
			if (event instanceof GraphSearchSolutionCandidateFoundEvent)
				return ((GraphSearchSolutionCandidateFoundEvent<NSrc,ASrc,V>)event).getSolutionCandidate();
		}
		throw new NoSuchElementException();
	}

	@Override
	public I getInput() {
		return problem;
	}
	
	@Override
	public GraphGenerator<NSrc,ASrc> getGraphGenerator() {
		return problem.getGraphGenerator();
	}
}
