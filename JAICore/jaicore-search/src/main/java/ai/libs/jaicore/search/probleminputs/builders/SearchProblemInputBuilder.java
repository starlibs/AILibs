package ai.libs.jaicore.search.probleminputs.builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.PathGoalTester;
import org.api4.java.datastructure.graph.IPath;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.NodeExpansionDescription;
import org.api4.java.datastructure.graph.implicit.RootGenerator;
import org.api4.java.datastructure.graph.implicit.SingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.SuccessorGenerator;

import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public abstract class SearchProblemInputBuilder<N, A, I extends GraphSearchInput<N, A>, B extends SearchProblemInputBuilder<N, A, I, B>> {

	private RootGenerator<N> rootGenerator;
	private SuccessorGenerator<N, A> successorGenerator;
	private PathGoalTester<N, A> goalTester;
	private IPath<N, A> prefixPath; // the path from the original root

	protected abstract B self(); // for the recursive generics

	public B withGraphGenerator(final IGraphGenerator<N, A> graphGenerator) {
		this.rootGenerator = graphGenerator.getRootGenerator();
		this.successorGenerator = graphGenerator.getSuccessorGenerator();
		return this.self();
	}

	public IGraphGenerator<N, A> getGraphGenerator() {
		return new IGraphGenerator<N, A>() {

			@Override
			public RootGenerator<N> getRootGenerator() {
				return SearchProblemInputBuilder.this.rootGenerator;
			}

			@Override
			public SuccessorGenerator<N, A> getSuccessorGenerator() {
				return SearchProblemInputBuilder.this.successorGenerator;
			}
		};
	}

	public B fromProblem(final GraphSearchInput<N, A> problem) {
		this.withGraphGenerator(problem.getGraphGenerator());
		this.withGoalTester(problem.getGoalTester());
		return this.self();
	}

	public B withRoot(final N root) {
		this.rootGenerator = () -> Arrays.asList(root);
		return this.self();
	}

	public void withSuccessorGenerator(final SuccessorGenerator<N, A> successorGenerator) {
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
			NodeExpansionDescription<N, A> ned = this.successorGenerator.generateSuccessors(current).get(child);
			current = ned.getTo();
			prefixArcs.add(ned.getAction());
			prefixNodes.add(current);
		}
		this.prefixPath = new SearchGraphPath<>(prefixNodes, prefixArcs);
		this.rootGenerator = new SingleRootGenerator<N>() {

			@Override
			public N getRoot() {
				return SearchProblemInputBuilder.this.prefixPath.getHead();
			}
		};
		return this.self();
	}

	public IPath<N, A> getPrefixPath() {
		return this.prefixPath;
	}

	public B withGoalTester(final PathGoalTester<N, A> goalTester) {
		this.goalTester = goalTester;
		return this.self();
	}

	public PathGoalTester<N, A> getGoalTester() {
		return this.goalTester;
	}

	public abstract I build();
}
