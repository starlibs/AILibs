package ai.libs.jaicore.search.probleminputs.builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.IRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;

import ai.libs.jaicore.search.model.other.SearchGraphPath;

public abstract class SearchProblemInputBuilder<N, A, I extends IPathSearchInput<N, A>, B extends SearchProblemInputBuilder<N, A, I, B>> {

	private IRootGenerator<N> rootGenerator;
	private ISuccessorGenerator<N, A> successorGenerator;
	private IPathGoalTester<N, A> goalTester;
	private ILabeledPath<N, A> prefixPath; // the path from the original root

	public B withGraphGenerator(final IGraphGenerator<N, A> graphGenerator) {
		this.rootGenerator = graphGenerator.getRootGenerator();
		this.successorGenerator = graphGenerator.getSuccessorGenerator();
		return this.self();
	}

	public IGraphGenerator<N, A> getGraphGenerator() {
		return new IGraphGenerator<N, A>() {

			@Override
			public IRootGenerator<N> getRootGenerator() {
				return SearchProblemInputBuilder.this.rootGenerator;
			}

			@Override
			public ISuccessorGenerator<N, A> getSuccessorGenerator() {
				return SearchProblemInputBuilder.this.successorGenerator;
			}
		};
	}

	public B fromProblem(final IPathSearchInput<N, A> problem) {
		this.withGraphGenerator(problem.getGraphGenerator());
		this.withGoalTester(problem.getGoalTester());
		return this.self();
	}

	public B withRoot(final N root) {
		this.rootGenerator = () -> Arrays.asList(root);
		return this.self();
	}

	public void withSuccessorGenerator(final ISuccessorGenerator<N, A> successorGenerator) {
		this.successorGenerator = successorGenerator;
	}

	/**
	 * Replaces the current root by a new one based on the successor generator
	 * @throws InterruptedException
	 **/
	public B withOffsetRoot(final List<Integer> indicesOfSuccessorsFromCurrentRoot) throws InterruptedException {
		if (this.rootGenerator == null) {
			throw new IllegalStateException("Cannot offset root when currently no root is set.");
		}
		if (this.successorGenerator == null) {
			throw new IllegalStateException("Cannot offset root when currently no successor generator is set.");
		}
		Collection<N> roots = this.rootGenerator.getRoots();
		if (roots.size() > 1) {
			throw new IllegalStateException("Root offset is a function that is only reasonably defined for problems with one root!");
		}
		List<N> prefixNodes = new ArrayList<>();
		List<A> prefixArcs = new ArrayList<>();
		N current = roots.iterator().next();
		prefixNodes.add(current);
		for (int child : indicesOfSuccessorsFromCurrentRoot) {
			INewNodeDescription<N, A> ned = this.successorGenerator.generateSuccessors(current).get(child);
			current = ned.getTo();
			prefixArcs.add(ned.getArcLabel());
			prefixNodes.add(current);
		}
		this.prefixPath = new SearchGraphPath<>(prefixNodes, prefixArcs);
		this.rootGenerator = new ISingleRootGenerator<N>() {

			@Override
			public N getRoot() {
				return SearchProblemInputBuilder.this.prefixPath.getHead();
			}
		};
		return this.self();
	}

	public ILabeledPath<N, A> getPrefixPath() {
		return this.prefixPath;
	}

	public B withGoalTester(final IPathGoalTester<N, A> goalTester) {
		this.goalTester = goalTester;
		return this.self();
	}

	public IPathGoalTester<N, A> getGoalTester() {
		return this.goalTester;
	}

	public abstract I build();

	protected abstract B self();
}
