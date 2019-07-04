package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import ai.libs.jaicore.search.core.interfaces.GraphGenerator;

public interface IPotentiallyGraphDependentNodeEvaluator<T, V extends Comparable<V>> extends INodeEvaluator<T, V> {
	public boolean requiresGraphGenerator();
	public void setGenerator(GraphGenerator<T, ?> generator);
}
