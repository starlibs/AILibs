package ai.libs.jaicore.search.probleminputs.builders;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IGraphGenerator;

import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public abstract class SearchProblemInputBuilder<N, A, I extends GraphSearchInput<N, A>> {
	
	private IGraphGenerator<N, A> graphGenerator;
	
	public void setGraphGenerator(IGraphGenerator<N, A> graphGenerator) {
		this.graphGenerator = graphGenerator;
	}
	public IGraphGenerator<N, A> getGraphGenerator() {
		return graphGenerator;
	}
	
	public abstract I build();
}
