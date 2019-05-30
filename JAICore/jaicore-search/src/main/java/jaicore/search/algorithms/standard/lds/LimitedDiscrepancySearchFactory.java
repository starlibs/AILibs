package jaicore.search.algorithms.standard.lds;

import jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithNodeRecommenderInput;

public class LimitedDiscrepancySearchFactory<N, A, V extends Comparable<V>> extends StandardORGraphSearchFactory<GraphSearchWithNodeRecommenderInput<N, A>, EvaluatedSearchGraphPath<N, A,V>, N, A, V> {

	@Override
	public LimitedDiscrepancySearch<N, A, V> getAlgorithm() {
		if (this.getInput() == null) {
			throw new IllegalArgumentException("Cannot create algorithm; problem input has not been set yet");
		}
		return this.getAlgorithm(this.getInput());
	}

	@Override
	public LimitedDiscrepancySearch<N,A,V> getAlgorithm(final GraphSearchWithNodeRecommenderInput<N, A> input) {
		return new LimitedDiscrepancySearch<>(input);
	}
}
