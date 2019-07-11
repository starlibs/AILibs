package ai.libs.jaicore.search.algorithms.standard.rstar;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IGraphGenerator;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeGoalTester;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.RootGenerator;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.SingleRootGenerator;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.SuccessorGenerator;

public class SubPathGraphGenerator<N, A> implements IGraphGenerator<N, A> {

	private final IGraphGenerator<N, A> gg;
	private final N from;
	private final N to;

	public SubPathGraphGenerator(final IGraphGenerator<N, A> gg, final N from, final N to) {
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
	public NodeGoalTester<N, A> getGoalTester() {
		return n -> n.equals(this.to);
	}
}
