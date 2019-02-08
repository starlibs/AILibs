package jaicore.search.algorithms.standard.bestfirst;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class StandardBestFirstFactory<N, A, V extends Comparable<V>> extends BestFirstFactory<GraphSearchWithSubpathEvaluationsInput<N, A, V>, N, A, V> {
	
	private INodeEvaluator<N, V> preferredNodeEvaluator;
	
	public void setNodeEvaluator(INodeEvaluator<N, V> nodeEvaluator) {
		setProblemInput(new GraphSearchWithSubpathEvaluationsInput<>(getInput() != null ? getInput().getGraphGenerator() : null, nodeEvaluator));
	}

	public void setGraphGenerator(GraphGenerator<N, A> graphGenerator) {
		setProblemInput(new GraphSearchWithSubpathEvaluationsInput<>(graphGenerator, getInput() != null ? getInput().getNodeEvaluator() : null));
	}

	public INodeEvaluator<N, V> getPreferredNodeEvaluator() {
		return preferredNodeEvaluator;
	}

	public void setPreferredNodeEvaluator(INodeEvaluator<N, V> preferredNodeEvaluator) {
		this.preferredNodeEvaluator = preferredNodeEvaluator;
	}
	
	@Override
	public BestFirst<GraphSearchWithSubpathEvaluationsInput<N, A, V>, N, A, V> getAlgorithm() {
		if (getInput().getGraphGenerator() == null)
			throw new IllegalStateException("Cannot produce BestFirst searches before the graph generator is set in the problem.");
		if (getInput().getNodeEvaluator() == null)
			throw new IllegalStateException("Cannot produce BestFirst searches before the node evaluator is set.");
		
		/* determine search problem */
		GraphSearchWithSubpathEvaluationsInput<N, A, V> problem = getInput();
		if (preferredNodeEvaluator != null) {
			problem = new GraphSearchWithSubpathEvaluationsInput<N, A, V>(problem.getGraphGenerator(), new AlternativeNodeEvaluator<>(preferredNodeEvaluator, problem.getNodeEvaluator()));
		}
		BestFirst<GraphSearchWithSubpathEvaluationsInput<N, A, V>, N, A, V> search = new BestFirst<>(problem);
		search.setTimeoutForComputationOfF(getTimeoutForFInMS(), getTimeoutEvaluator());
		if (getLoggerName() != null && getLoggerName().length() > 0)
			search.setLoggerName(getLoggerName());
		return search;
	}
}
