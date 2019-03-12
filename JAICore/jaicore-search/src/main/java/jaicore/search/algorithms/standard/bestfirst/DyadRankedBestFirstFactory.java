package jaicore.search.algorithms.standard.bestfirst;

import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class DyadRankedBestFirstFactory<N, A, V extends Comparable<V>> extends StandardBestFirstFactory<N, A, V> {
	

	private IBestFirstQueueConfiguration<GraphSearchWithSubpathEvaluationsInput<N,A,V>, N, A, V> OPENConfig;

	public DyadRankedBestFirstFactory (IBestFirstQueueConfiguration<GraphSearchWithSubpathEvaluationsInput<N,A,V>, N, A, V> OPENConfig) {
			this.OPENConfig = OPENConfig;
	}

	@Override
	public BestFirst<GraphSearchWithSubpathEvaluationsInput<N, A, V>, N, A, V> getAlgorithm() {
		BestFirst<GraphSearchWithSubpathEvaluationsInput<N, A, V>,N,A,V> bestFirst = super.getAlgorithm();
		OPENConfig.configureBestFirst(bestFirst);
		return bestFirst;
	}
}
