package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.datastructure.graph.ILabeledPath;

public class RandomizedDepthFirstNodeEvaluator<T, A> implements IPathEvaluator<T, A, Double> {

	private final Random rand;

	public RandomizedDepthFirstNodeEvaluator(final Random rand) {
		super();
		this.rand = rand;
	}

	@Override
	public Double evaluate(final ILabeledPath<T, A> node) {
		return (double) (-1 * (node.getNodes().size() * 1000 + this.rand.nextInt(100)));
	}
}
