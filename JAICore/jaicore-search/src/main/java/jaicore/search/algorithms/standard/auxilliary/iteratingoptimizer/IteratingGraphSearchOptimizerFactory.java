package jaicore.search.algorithms.standard.auxilliary.iteratingoptimizer;

import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class IteratingGraphSearchOptimizerFactory<I extends GraphSearchWithPathEvaluationsInput<N, A, V>, N, A, V extends Comparable<V>> extends StandardORGraphSearchFactory<I, EvaluatedSearchGraphPath<N, A, V>, N, A, V>
		implements IOptimalPathInORGraphSearchFactory<I, N, A, V> {

	private IGraphSearchFactory<GraphSearchInput<N, A>, SearchGraphPath<N, A>, N, A> baseAlgorithmFactory;

	public IteratingGraphSearchOptimizerFactory() {
		super();
	}

	public IteratingGraphSearchOptimizerFactory(IGraphSearchFactory<GraphSearchInput<N, A>, SearchGraphPath<N, A>, N, A> baseAlgorithmFactory) {
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
	public IteratingGraphSearchOptimizer<I, N, A, V> getAlgorithm(I input) {
		if (baseAlgorithmFactory == null) {
			throw new IllegalStateException("Cannot produce " + IteratingGraphSearchOptimizer.class + " searches before the factory for the base search algorithm has been set.");
		}
		return new IteratingGraphSearchOptimizer<>(input, baseAlgorithmFactory.getAlgorithm(new GraphSearchInput<>(input.getGraphGenerator())));
	}

	public IGraphSearchFactory<GraphSearchInput<N, A>, SearchGraphPath<N, A>, N, A> getBaseAlgorithmFactory() {
		return baseAlgorithmFactory;
	}

	public void setBaseAlgorithmFactory(IGraphSearchFactory<GraphSearchInput<N, A>, SearchGraphPath<N, A>, N, A> baseAlgorithmFactory) {
		this.baseAlgorithmFactory = baseAlgorithmFactory;
	}
}
