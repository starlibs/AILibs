package jaicore.search.model.probleminputs.builders;

import jaicore.search.core.interfaces.GraphGenerator;

public abstract class SearchProblemInputBuilder<N, A, I> {
	
	private GraphGenerator<N, A> graphGenerator;
	
	public void setGraphGenerator(GraphGenerator<N, A> graphGenerator) {
		this.graphGenerator = graphGenerator;
	}
	public GraphGenerator<N, A> getGraphGenerator() {
		return graphGenerator;
	}
	
	public abstract I build();
}
