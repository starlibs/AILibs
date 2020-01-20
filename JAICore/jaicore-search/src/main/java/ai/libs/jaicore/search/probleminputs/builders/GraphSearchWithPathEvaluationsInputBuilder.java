package ai.libs.jaicore.search.probleminputs.builders;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class GraphSearchWithPathEvaluationsInputBuilder<N, A, V extends Comparable<V>> extends SearchProblemInputBuilder<N, A, IPathSearchWithPathEvaluationsInput<N, A, V>, GraphSearchWithPathEvaluationsInputBuilder<N, A, V>> {

	private IPathEvaluator<N, A, V> pathEvaluator;

	public GraphSearchWithPathEvaluationsInputBuilder() {

	}

	public GraphSearchWithPathEvaluationsInputBuilder(final IPathEvaluator<N, A, V> nodeEvaluator) {
		super();
		this.pathEvaluator = nodeEvaluator;
	}


	@Override
	public GraphSearchWithPathEvaluationsInputBuilder<N, A, V> fromProblem(final IPathSearchInput<N, A> problem) {
		super.fromProblem(problem);
		if (problem instanceof GraphSearchWithPathEvaluationsInput) {
			this.withPathEvaluator(((GraphSearchWithPathEvaluationsInput<N, A, V>) problem).getPathEvaluator());
		}
		return this;
	}

	public IPathEvaluator<N, A, V> getPathEvaluator() {
		return this.pathEvaluator;
	}

	public GraphSearchWithPathEvaluationsInputBuilder<N, A, V> withPathEvaluator(final IPathEvaluator<N, A, V> pathEvaluator) {
		this.pathEvaluator = pathEvaluator;
		return this;
	}

	@Override
	public IPathSearchWithPathEvaluationsInput<N, A, V> build() {
		return new GraphSearchWithPathEvaluationsInput<>(this.getGraphGenerator(), this.getGoalTester(), this.pathEvaluator);
	}

	@Override
	protected GraphSearchWithPathEvaluationsInputBuilder<N, A, V> self() {
		return this;
	}
}
