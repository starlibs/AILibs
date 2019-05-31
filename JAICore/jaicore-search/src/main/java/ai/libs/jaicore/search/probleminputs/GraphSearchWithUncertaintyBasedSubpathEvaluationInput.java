package ai.libs.jaicore.search.probleminputs;

import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.IPotentiallyUncertaintyAnnotatingNodeEvaluator;
import ai.libs.jaicore.search.core.interfaces.GraphGenerator;

public class GraphSearchWithUncertaintyBasedSubpathEvaluationInput<N, A, V extends Comparable<V>> extends GraphSearchWithSubpathEvaluationsInput<N, A, V> {
	
	public GraphSearchWithUncertaintyBasedSubpathEvaluationInput(GraphGenerator<N, A> graphGenerator, IPotentiallyUncertaintyAnnotatingNodeEvaluator<N, V> nodeEvaluator) {
		super(graphGenerator, nodeEvaluator);
	}

}
