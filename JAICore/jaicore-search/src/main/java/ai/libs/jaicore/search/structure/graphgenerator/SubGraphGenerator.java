package ai.libs.jaicore.search.structure.graphgenerator;

import ai.libs.jaicore.search.core.interfaces.GraphGenerator;

/**
 * This is a graph generator that takes another graph generator and generates its sub-graph under a given root node
 *
 * @author fmohr
 *
 * @param <N>
 * @param <A>
 */
public class SubGraphGenerator<N,A> implements GraphGenerator<N, A> {

	private final GraphGenerator<N,A> actualGraphGenerator;
	private final N newRoot;

	public SubGraphGenerator(final GraphGenerator<N, A> actualGraphGenerator, final N newRoot) {
		super();
		this.actualGraphGenerator = actualGraphGenerator;
		this.newRoot = newRoot;
	}

	@Override
	public SingleRootGenerator<N> getRootGenerator() {
		return () -> this.newRoot;
	}

	@Override
	public SuccessorGenerator<N, A> getSuccessorGenerator() {
		return this.actualGraphGenerator.getSuccessorGenerator();
	}

	@Override
	public GoalTester<N> getGoalTester() {
		return this.actualGraphGenerator.getGoalTester();
	}
}
