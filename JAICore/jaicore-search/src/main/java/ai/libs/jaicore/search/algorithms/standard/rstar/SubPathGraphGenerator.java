package ai.libs.jaicore.search.algorithms.standard.rstar;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.RootGenerator;
import org.api4.java.datastructure.graph.implicit.SingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.SuccessorGenerator;

public class SubPathGraphGenerator<N, A> implements IGraphGenerator<N, A> {

	private final IGraphGenerator<N, A> gg;
	private final N from;

	public SubPathGraphGenerator(final IGraphGenerator<N, A> gg, final N from) {
		super();
		this.gg = gg;
		this.from = from;
	}

	@Override
	public RootGenerator<N> getRootGenerator() {
		return (SingleRootGenerator<N>)(() -> this.from);
	}

	@Override
	public SuccessorGenerator<N, A> getSuccessorGenerator() {
		return this.gg.getSuccessorGenerator();
	}
}
