package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.IPath;

public class SkippingNodeEvaluator<T, A, V extends Comparable<V>> implements IPathEvaluator<T, A, V> {

	private final IPathEvaluator<T,A, V> actualEvaluator;
	private final Random rand;
	private final float coin;
	private final Map<IPath<T, A>, V> fCache = new HashMap<>();

	public SkippingNodeEvaluator(final IPathEvaluator<T, A, V> actualEvaluator, final Random rand, final float coin) {
		super();
		this.actualEvaluator = actualEvaluator;
		this.rand = rand;
		this.coin = coin;
	}

	@Override
	public V f(final IPath<T, A> path) throws PathEvaluationException, InterruptedException {
		int depth = path.getNodes().size() - 1;
		if (!this.fCache.containsKey(path)) {
			if (depth == 0) {
				this.fCache.put(path, this.actualEvaluator.f(path));
			} else {
				if (this.rand.nextFloat() >= this.coin) {
					this.fCache.put(path, this.actualEvaluator.f(path));
				} else {
					this.fCache.put(path, this.f(path.getPathToParentOfHead()));
				}
			}
		}
		return this.fCache.get(path);
	}

	@Override
	public String toString() {
		return "SkippingEvaluator [actualEvaluator=" + this.actualEvaluator + "]";
	}
}