package jaicore.search.probleminputs;

import java.util.Comparator;

import jaicore.search.core.interfaces.GraphGenerator;

public class GraphSearchWithNodeRecommenderInput<N, A> extends GraphSearchInput<N, A> {
	private final Comparator<N> recommender;

	public GraphSearchWithNodeRecommenderInput(GraphGenerator<N, A> graphGenerator, Comparator<N> recommender) {
		super(graphGenerator);
		this.recommender = recommender;
	}

	public Comparator<N> getRecommender() {
		return recommender;
	}
}
