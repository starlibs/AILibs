package jaicore.search.probleminputs;

import jaicore.search.core.interfaces.GraphGenerator;

/**
 * This input is provided to algorithms that should find a solution path in a graph without path cost.
 * That is, the set of solutions is exactly the set of paths from the root to a goal node.
 * 
 * @author fmohr
 *
 * @param <N>
 * @param <A>
 */
public class GraphSearchInput<N, A> {
	private final GraphGenerator<N, A> graphGenerator;

	public GraphSearchInput(GraphGenerator<N, A> graphGenerator) {
		super();
		this.graphGenerator = graphGenerator;
	}

	public GraphGenerator<N, A> getGraphGenerator() {
		return graphGenerator;
	}
}
