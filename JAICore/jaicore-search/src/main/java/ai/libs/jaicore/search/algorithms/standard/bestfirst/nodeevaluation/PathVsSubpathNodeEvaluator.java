package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

public class PathVsSubpathNodeEvaluator<N, A, V extends Comparable<V>> extends AlternativeNodeEvaluator<N, A, V> {

	public PathVsSubpathNodeEvaluator(final IPathEvaluator<N, A, V> goalPathEvaluator, final IPathEvaluator<N, A, V> subPathEvaluator, final IPathGoalTester<N, A> goalTester) {
		super(p -> goalTester.isGoal(p) ? goalPathEvaluator.evaluate(p) : null, subPathEvaluator);
	}

}
