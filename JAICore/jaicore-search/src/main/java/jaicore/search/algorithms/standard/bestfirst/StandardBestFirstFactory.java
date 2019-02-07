package jaicore.search.algorithms.standard.bestfirst;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class StandardBestFirstFactory<N,A,V extends Comparable<V>> extends BestFirstFactory<GraphSearchWithSubpathEvaluationsInput<N, A, V>, N, A, V> {

		public void setNodeEvaluator(INodeEvaluator<N, V> nodeEvaluator) {
		setProblemInput(new GraphSearchWithSubpathEvaluationsInput<>(getInput() != null ? getInput().getGraphGenerator() : null, nodeEvaluator));
	}
	
	public void setGraphGenerator(GraphGenerator<N, A> graphGenerator) {
		setProblemInput(new GraphSearchWithSubpathEvaluationsInput<>(graphGenerator, getInput() != null ? getInput().getNodeEvaluator() : null));
	}
}
