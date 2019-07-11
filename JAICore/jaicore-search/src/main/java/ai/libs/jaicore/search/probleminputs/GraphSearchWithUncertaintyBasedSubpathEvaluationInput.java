package ai.libs.jaicore.search.probleminputs;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IGraphGenerator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallyUncertaintyAnnotatingPathEvaluator;

public class GraphSearchWithUncertaintyBasedSubpathEvaluationInput<N, A, V extends Comparable<V>> extends GraphSearchWithSubpathEvaluationsInput<N, A, V> {

	public GraphSearchWithUncertaintyBasedSubpathEvaluationInput(final IGraphGenerator<N, A> graphGenerator, final IPotentiallyUncertaintyAnnotatingPathEvaluator<N, A, V> nodeEvaluator) {
		super(graphGenerator, nodeEvaluator);
	}

}
