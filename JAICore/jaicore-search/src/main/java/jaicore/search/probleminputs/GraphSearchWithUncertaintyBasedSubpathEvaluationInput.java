package jaicore.search.probleminputs;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.IPotentiallyUncertaintyAnnotatingNodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;

public class GraphSearchWithUncertaintyBasedSubpathEvaluationInput<N, A, V extends Comparable<V>> extends GraphSearchWithSubpathEvaluationsInput<N, A, V> {
	
	public GraphSearchWithUncertaintyBasedSubpathEvaluationInput(GraphGenerator<N, A> graphGenerator, IPotentiallyUncertaintyAnnotatingNodeEvaluator<N, V> nodeEvaluator) {
		super(graphGenerator, nodeEvaluator);
	}

}
