package ai.libs.jaicore.search.probleminputs;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallyUncertaintyAnnotatingPathEvaluator;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

public class GraphSearchWithUncertaintyBasedSubpathEvaluationInput<N, A, V extends Comparable<V>> extends GraphSearchWithSubpathEvaluationsInput<N, A, V> {

	public GraphSearchWithUncertaintyBasedSubpathEvaluationInput(final IPathSearchInput<N, A> baseProblem, final IPotentiallyUncertaintyAnnotatingPathEvaluator<N, A, V> nodeEvaluator) {
		super(baseProblem, nodeEvaluator);
	}

	public GraphSearchWithUncertaintyBasedSubpathEvaluationInput(final IGraphGenerator<N, A> graphGenerator, final IPathGoalTester<N, A> goalTester, final IPotentiallyUncertaintyAnnotatingPathEvaluator<N, A, V> nodeEvaluator) {
		super(graphGenerator, goalTester, nodeEvaluator);
	}

}
