package ai.libs.jaicore.search.probleminputs;

import org.api4.java.ai.graphsearch.problem.IGraphSearchInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.PathGoalTester;
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
public class GraphSearchInput<N, A> implements IGraphSearchInput<N, A> {
	private final IGraphGenerator<N, A> graphGenerator;
	private final PathGoalTester<N, A> goalTester;

	public GraphSearchInput(final IGraphSearchInput<N, A> inputToClone) {
		this(inputToClone.getGraphGenerator(), inputToClone.getGoalTester());
	}

	public GraphSearchInput(final IGraphGenerator<N, A> graphGenerator, final PathGoalTester<N, A> goalTester) {
		super();
		this.graphGenerator = graphGenerator;
		this.goalTester = goalTester;
	}

	@Override
	public IGraphGenerator<N, A> getGraphGenerator() {
		return this.graphGenerator;
	}

	@Override
	public PathGoalTester<N, A> getGoalTester() {
		return this.goalTester;
	}
}
