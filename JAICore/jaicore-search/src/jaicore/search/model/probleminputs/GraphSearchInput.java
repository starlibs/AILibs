package jaicore.search.model.probleminputs;

import jaicore.search.core.interfaces.GraphGenerator;

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
