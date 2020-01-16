package ai.libs.jaicore.search.algorithms.standard.lds;

import ai.libs.jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNodeRecommenderInput;

public class BestFirstLimitedDiscrepancySearchFactory<I extends GraphSearchWithNodeRecommenderInput<N, A>, N, A, V extends Comparable<V>>
extends StandardORGraphSearchFactory<I, EvaluatedSearchGraphPath<N, A, V>, N, A, V, BestFirstLimitedDiscrepancySearch<I, N, A, V>> {

	@Override
	public BestFirstLimitedDiscrepancySearch<I, N, A, V> getAlgorithm() {
		if (this.getInput() == null) {
			throw new IllegalArgumentException("Cannot create algorithm; problem input has not been set yet");
		}
		return this.getAlgorithm(this.getInput());
	}

	@Override
	public BestFirstLimitedDiscrepancySearch<I, N, A, V> getAlgorithm(final I input) {
		return new BestFirstLimitedDiscrepancySearch<>(input);
	}
}
