package jaicore.search.probleminputs.builders;

import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.probleminputs.GraphSearchInput;

public abstract class SearchProblemInputBuilder<N, A, I extends GraphSearchInput<N, A>> {
	
	private GraphGenerator<N, A> graphGenerator;
	
	public void setGraphGenerator(GraphGenerator<N, A> graphGenerator) {
		this.graphGenerator = graphGenerator;
	}
	public GraphGenerator<N, A> getGraphGenerator() {
		return graphGenerator;
	}
	
	public abstract I build();
}
