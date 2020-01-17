package ai.libs.jaicore.search.exampleproblems.randomtrees;

import java.util.List;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

public class RandomTreeSearchProblem implements IPathSearchWithPathEvaluationsInput<List<Integer>, Integer, Double> {

	private final int b;
	private final int d;
	private final long seed;
	private final IGraphGenerator<List<Integer>, Integer> gg;
	private final IPathGoalTester<List<Integer>, Integer> gt;
	private final IPathEvaluator<List<Integer>, Integer, Double> se;
	private final boolean scoresPerEdge;

	public RandomTreeSearchProblem(final int b, final int d, final long seed, final int maxPerDepth, final boolean scoresPerEdge) {
		super();
		this.b = b;
		this.d = d;
		this.seed = seed;
		this.gg = new RandomTreeGraphGenerator(b, d, seed, maxPerDepth);
		this.gt = new RandomTreeGoalTester(d);
		this.scoresPerEdge = scoresPerEdge;
		this.se = n -> scoresPerEdge ? ((double)n.getHead().stream().reduce((current, added) -> current + added).get()) / (d * maxPerDepth) : n.getHead().get(n.getHead().size() - 1);
	}

	@Override
	public IGraphGenerator<List<Integer>, Integer> getGraphGenerator() {
		return this.gg;
	}

	@Override
	public IPathGoalTester<List<Integer>, Integer> getGoalTester() {
		return this.gt;
	}

	@Override
	public IPathEvaluator<List<Integer>, Integer, Double> getPathEvaluator() {
		return this.se;
	}

	public int getB() {
		return this.b;
	}

	public int getD() {
		return this.d;
	}

	public IGraphGenerator<List<Integer>, Integer> getGg() {
		return this.gg;
	}

	public IPathGoalTester<List<Integer>, Integer> getGt() {
		return this.gt;
	}

	public IPathEvaluator<List<Integer>, Integer, Double> getSe() {
		return this.se;
	}

	public boolean isScoresPerEdge() {
		return this.scoresPerEdge;
	}

	public long getSeed() {
		return this.seed;
	}
}
