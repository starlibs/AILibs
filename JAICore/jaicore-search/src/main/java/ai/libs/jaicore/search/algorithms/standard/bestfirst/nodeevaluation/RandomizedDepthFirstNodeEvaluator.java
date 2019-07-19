package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.SerializableNodeEvaluator;
import org.api4.java.datastructure.graph.IPath;

@SuppressWarnings("serial")
public class RandomizedDepthFirstNodeEvaluator<T, A> implements SerializableNodeEvaluator<T, A, Double> {

	private final Random rand;

	public RandomizedDepthFirstNodeEvaluator(final Random rand) {
		super();
		this.rand = rand;
	}

	@Override
	public Double f(final IPath<T, A> node) {
		return (double) (-1 * (node.getNodes().size() * 1000 + this.rand.nextInt(100)));
	}
}
