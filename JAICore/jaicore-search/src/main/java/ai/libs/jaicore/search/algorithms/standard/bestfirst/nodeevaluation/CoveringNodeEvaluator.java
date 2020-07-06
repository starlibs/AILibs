package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.common.control.ILoggingCustomizable;

/**
 * This node evaluator allows to use pair of two node evaluators of which the first is HIDDEN by the second.
 * The first evaluator is executed and its events are published via the event bus, but its result is not returned.
 *
 * This can be useful, for example, to collect solutions via a random completions within a node evaluation but using a different computation to return the true score.
 * A typical case of application is a Branch and Bound algorithm, where the returned value is an optimistic heuristic, but a set of solutions is computed to maybe
 * register new best solutions.
 *
 * @author Felix Mohr
 *
 * @param <N>
 * @param <V>
 */
public class CoveringNodeEvaluator<N, A, V extends Comparable<V>> extends AlternativeNodeEvaluator<N, A, V> implements ILoggingCustomizable {

	public CoveringNodeEvaluator(final IPathEvaluator<N, A, V> ne1, final IPathEvaluator<N, A, V> ne2) {
		super(ne1, ne2, true);
	}
}
