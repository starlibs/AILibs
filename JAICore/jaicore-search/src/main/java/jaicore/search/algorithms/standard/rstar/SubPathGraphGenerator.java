package jaicore.search.algorithms.standard.rstar;

import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class SubPathGraphGenerator<N, A> implements GraphGenerator<N, A> {

	private final GraphGenerator<N, A> gg;
	private final N from;
	private final N to;

	public SubPathGraphGenerator(final GraphGenerator<N, A> gg, final N from, final N to) {
		super();
		this.gg = gg;
		this.from = from;
		this.to = to;
	}

	@Override
	public RootGenerator<N> getRootGenerator() {
		return (SingleRootGenerator<N>)(() -> this.from);
	}

	@Override
	public SuccessorGenerator<N, A> getSuccessorGenerator() {
		return this.gg.getSuccessorGenerator();
	}

	@Override
	public GoalTester<N> getGoalTester() {
		return (NodeGoalTester<N>)(n -> n.equals(this.to));
	}

	@Override
	public boolean isSelfContained() {
		return false;
	}

	@Override
	public void setNodeNumbering(final boolean nodenumbering) {

		/* does not matter */
	}

}
