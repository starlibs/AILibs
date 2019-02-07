package hasco.variants.forwarddecomposition;

import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import jaicore.ml.dyadranking.search.ADyadRankedNodeQueueConfig;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.problemtransformers.GraphSearchProblemInputToUninformedGeneralEvaluatedTraversalTree;

/**
 * HASCO that does not do random completions and instead uses dyad ranking to
 * sort the OPEN list for best first.
 * 
 * @author Helena Graf
 *
 */
public class HASCOViaFDAndBestFirstWithDyadRanking extends HASCOViaFDAndBestFirst<Double> {

	/**
	 * Create a new HASCO with the given problem and best first node queue
	 * configuration
	 * 
	 * @param configurationProblem
	 * @param bestFirstNodeQueueConfig
	 */
	public HASCOViaFDAndBestFirstWithDyadRanking(
			RefinementConfiguredSoftwareConfigurationProblem<Double> configurationProblem,
			ADyadRankedNodeQueueConfig<TFDNode> bestFirstNodeQueueConfig) {
		super(configurationProblem,
				new GraphSearchProblemInputToUninformedGeneralEvaluatedTraversalTree<TFDNode, String>(),
				bestFirstNodeQueueConfig);
	}

}
