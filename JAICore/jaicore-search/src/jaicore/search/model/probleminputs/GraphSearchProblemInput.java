package jaicore.search.model.probleminputs;

import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.ISolutionEvaluator;

/**
 * In AILibs, a graph search problem always aims at identifying one or more paths from
 * a set of root nodes to a goal node. Usually, such paths are associated with a value
 * that qualifies them.
 * 
 * This is the most general problem input one can have if there is no other knowledge.
 * 
 * @author fmohr
 *
 * @param <N>
 * @param <A>
 * @param <V>
 */
public class GraphSearchProblemInput<N, A, V extends Comparable<V>> extends GraphSearchInput<N, A> {
	private final ISolutionEvaluator<N, V> pathEvaluator;

	public GraphSearchProblemInput(GraphGenerator<N, A> graphGenerator, ISolutionEvaluator<N, V> pathEvaluator) {
		super(graphGenerator);
		this.pathEvaluator = pathEvaluator;
	}

	public ISolutionEvaluator<N, V> getPathEvaluator() {
		return pathEvaluator;
	}
}
