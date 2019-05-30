package jaicore.search.structure.graphgenerator;

import jaicore.search.core.interfaces.GraphGenerator;

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

	public SubGraphGenerator(GraphGenerator<N, A> actualGraphGenerator, N newRoot) {
		super();
		this.actualGraphGenerator = actualGraphGenerator;
		this.newRoot = newRoot;
	}

	@Override
	public SingleRootGenerator<N> getRootGenerator() {
		return () -> newRoot;
	}

	@Override
	public SuccessorGenerator<N, A> getSuccessorGenerator() {
		return actualGraphGenerator.getSuccessorGenerator();
	}

	@Override
	public GoalTester<N> getGoalTester() {
		return actualGraphGenerator.getGoalTester();
	}

	@Override
	public boolean isSelfContained() {
		return actualGraphGenerator.isSelfContained();
	}

	@Override
	public void setNodeNumbering(boolean nodenumbering) {
		throw new UnsupportedOperationException("Node numering cannot be modified on sub graph generator");
	}
}
