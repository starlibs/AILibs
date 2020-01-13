package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;

public class TimeLoggingNodeEvaluator<T, A, V extends Comparable<V>> extends DecoratingNodeEvaluator<T, A, V> {

	private final Map<ILabeledPath<T, A>, Integer> times = new ConcurrentHashMap<>();

	public TimeLoggingNodeEvaluator(final IPathEvaluator<T, A,V> baseEvaluator) {
		super(baseEvaluator);
	}

	public int getMSRequiredForComputation(final ILabeledPath<T, A> path) {
		if (!this.times.containsKey(path)) {
			throw new IllegalArgumentException("No f-value has been computed for node: " + path);
		}
		return this.times.get(path);
	}

	@Override
	public V evaluate(final ILabeledPath<T, A> path) throws PathEvaluationException, InterruptedException {
		long start = System.currentTimeMillis();
		V f = super.evaluate(path);
		this.times.put(path, (int) (System.currentTimeMillis() - start));
		return f;
	}
}
