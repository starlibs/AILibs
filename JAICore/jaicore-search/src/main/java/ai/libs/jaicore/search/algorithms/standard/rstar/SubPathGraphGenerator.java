package ai.libs.jaicore.search.algorithms.standard.rstar;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.IRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;

public class SubPathGraphGenerator<N, A> implements IGraphGenerator<N, A> {

	private final IGraphGenerator<N, A> gg;
	private final N from;

	public SubPathGraphGenerator(final IGraphGenerator<N, A> gg, final N from) {
		super();
		this.gg = gg;
		this.from = from;
	}

	@Override
	public IRootGenerator<N> getRootGenerator() {
		return (ISingleRootGenerator<N>)(() -> this.from);
	}

	@Override
	public ISuccessorGenerator<N, A> getSuccessorGenerator() {
		return this.gg.getSuccessorGenerator();
	}
}
