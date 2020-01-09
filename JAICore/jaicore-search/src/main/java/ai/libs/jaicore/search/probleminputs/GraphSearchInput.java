package ai.libs.jaicore.search.probleminputs;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

/**
 * This input is provided to algorithms that should find a solution path in a graph without path cost.
 * That is, the set of solutions is exactly the set of paths from the root to a goal node.
 *
 * @author fmohr
 *
 * @param <N>
 * @param <A>
 */
public class GraphSearchInput<N, A> implements IPathSearchInput<N, A> {
	private final IGraphGenerator<N, A> graphGenerator;
	private final IPathGoalTester<N, A> goalTester;

	public GraphSearchInput(final IPathSearchInput<N, A> inputToClone) {
		this(inputToClone.getGraphGenerator(), inputToClone.getGoalTester());
	}

	public GraphSearchInput(final IGraphGenerator<N, A> graphGenerator, final IPathGoalTester<N, A> goalTester) {
		super();
		this.graphGenerator = graphGenerator;
		this.goalTester = goalTester;
	}

	@Override
	public IGraphGenerator<N, A> getGraphGenerator() {
		return this.graphGenerator;
	}

	@Override
	public IPathGoalTester<N, A> getGoalTester() {
		return this.goalTester;
	}
}
