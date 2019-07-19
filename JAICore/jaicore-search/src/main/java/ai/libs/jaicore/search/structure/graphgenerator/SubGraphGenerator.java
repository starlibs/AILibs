package ai.libs.jaicore.search.structure.graphgenerator;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.SingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.SuccessorGenerator;

/**
 * This is a graph generator that takes another graph generator and generates its sub-graph under a given root node
 *
 * @author fmohr
 *
 * @param <N>
 * @param <A>
 */
public class SubGraphGenerator<N, A> implements IGraphGenerator<N, A> {

	private final IGraphGenerator<N, A> actualGraphGenerator;
	private final N newRoot;

	public SubGraphGenerator(final IGraphGenerator<N, A> actualGraphGenerator, final N newRoot) {
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
}
