package ai.libs.jaicore.search.probleminputs;

import java.util.Comparator;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

public class GraphSearchWithNodeRecommenderInput<N, A> extends GraphSearchInput<N, A> {
	private final Comparator<N> recommender;

	public GraphSearchWithNodeRecommenderInput(final IPathSearchInput<N, A> graphSearchInput, final Comparator<N> recommender) {
		super(graphSearchInput);
		this.recommender = recommender;
	}

	public GraphSearchWithNodeRecommenderInput(final IGraphGenerator<N, A> graphGenerator, final IPathGoalTester<N, A> goalTester, final Comparator<N> recommender) {
		super(graphGenerator, goalTester);
		this.recommender = recommender;
	}

	public Comparator<N> getRecommender() {
		return this.recommender;
	}
}
