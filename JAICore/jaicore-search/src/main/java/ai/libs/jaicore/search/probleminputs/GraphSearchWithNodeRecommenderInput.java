package ai.libs.jaicore.search.probleminputs;

import java.util.Comparator;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IGraphGenerator;

public class GraphSearchWithNodeRecommenderInput<N, A> extends GraphSearchInput<N, A> {
	private final Comparator<N> recommender;

	public GraphSearchWithNodeRecommenderInput(IGraphGenerator<N, A> graphGenerator, Comparator<N> recommender) {
		super(graphGenerator);
		this.recommender = recommender;
	}

	public Comparator<N> getRecommender() {
		return recommender;
	}
}
