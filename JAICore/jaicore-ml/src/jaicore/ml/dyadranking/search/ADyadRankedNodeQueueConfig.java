package jaicore.ml.dyadranking.search;

import jaicore.ml.dyadranking.algorithm.ADyadRanker;
import jaicore.search.algorithms.standard.bestfirst.IBestFirstQueueConfiguration;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;

/**
 * A configuration for a dyad ranked node queue. Mainly configures the dyad
 * ranker.
 * 
 * @author Helena Graf
 *
 * @param <N>
 */
public abstract class ADyadRankedNodeQueueConfig<N>
		implements IBestFirstQueueConfiguration<GeneralEvaluatedTraversalTree<N, String, Double>, N, String, Double> {

	/**
	 * the ranker used to rank dyads consisting of pipeline metafeatures and dataset
	 * metafeatures
	 */
	ADyadRanker ranker;

	/**
	 * Construct a new dyad ranking node queue configuration.
	 */
	public ADyadRankedNodeQueueConfig() {
		// TODO: initialize Ranker
	}
}
