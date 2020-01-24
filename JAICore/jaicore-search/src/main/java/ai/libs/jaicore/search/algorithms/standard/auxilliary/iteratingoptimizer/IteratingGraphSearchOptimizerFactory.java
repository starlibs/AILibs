package ai.libs.jaicore.search.algorithms.standard.auxilliary.iteratingoptimizer;

import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearchFactory;
import org.api4.java.ai.graphsearch.problem.IPathSearchFactory;
import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

import ai.libs.jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

public class IteratingGraphSearchOptimizerFactory<I extends IPathSearchWithPathEvaluationsInput<N, A, V>, N, A, V extends Comparable<V>> extends StandardORGraphSearchFactory<I, EvaluatedSearchGraphPath<N, A, V>, N, A, V, IteratingGraphSearchOptimizer<I, N, A, V>>
implements IOptimalPathInORGraphSearchFactory<I, EvaluatedSearchGraphPath<N, A, V>, N, A, V, IteratingGraphSearchOptimizer<I, N, A, V>> {

	private IPathSearchFactory<IPathSearchInput<N, A>, SearchGraphPath<N, A>, N, A, ?> baseAlgorithmFactory;

	public IteratingGraphSearchOptimizerFactory() {
		super();
	}

	public IteratingGraphSearchOptimizerFactory(final IPathSearchFactory<IPathSearchInput<N, A>, SearchGraphPath<N, A>, N, A, ?> baseAlgorithmFactory) {
		super();
		this.baseAlgorithmFactory = baseAlgorithmFactory;
	}

	@Override
	public IteratingGraphSearchOptimizer<I, N, A, V> getAlgorithm() {
		if (this.getInput().getGraphGenerator() == null) {
			throw new IllegalStateException("Cannot produce " + IteratingGraphSearchOptimizer.class + " searches before the graph generator is set in the problem.");
		}
		return this.getAlgorithm(this.getInput());
	}

	@Override
	public IteratingGraphSearchOptimizer<I, N, A, V> getAlgorithm(final I input) {
		if (this.baseAlgorithmFactory == null) {
			throw new IllegalStateException("Cannot produce " + IteratingGraphSearchOptimizer.class + " searches before the factory for the base search algorithm has been set.");
		}
		return new IteratingGraphSearchOptimizer<>(input, this.baseAlgorithmFactory.getAlgorithm(input));
	}

	public IPathSearchFactory<IPathSearchInput<N, A>, SearchGraphPath<N, A>, N, A, ?> getBaseAlgorithmFactory() {
		return this.baseAlgorithmFactory;
	}

	public void setBaseAlgorithmFactory(final IPathSearchFactory<IPathSearchInput<N, A>, SearchGraphPath<N, A>, N, A, ?> baseAlgorithmFactory) {
		this.baseAlgorithmFactory = baseAlgorithmFactory;
	}
}
