package ai.libs.jaicore.search.algorithms.standard.awastar;

import ai.libs.jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class AWAStarFactory<I extends GraphSearchWithSubpathEvaluationsInput<N, A, V>, N, A, V extends Comparable<V>>
extends StandardORGraphSearchFactory<I, EvaluatedSearchGraphPath<N, A, V>, N, A, V> {

	@Override
	public AwaStarSearch<I, N, A, V> getAlgorithm() {
		return this.getAlgorithm(this.getInput());
	}

	@Override
	public AwaStarSearch<I, N, A, V> getAlgorithm(final I input) {
		return new AwaStarSearch<>(input);
	}

}
