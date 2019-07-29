package ai.libs.jaicore.search.model.other;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.datastructure.graph.IPath;

public class AgnosticPathEvaluator<N, A> implements IPathEvaluator<N, A, Double> {

	@Override
	public Double evaluate(final IPath<N, A> solutionPath) {
		return 0.0;
	}
}
