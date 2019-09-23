package ai.libs.hasco.variants.forwarddecomposition;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.DyadRankedBestFirstFactory;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.IBestFirstQueueConfiguration;
import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer;

/**
 * HASCO variant factory using best first and a dyad-ranked OPEN list.
 *
 * @author Helena Graf
 *
 */
public class HASCOViaFDAndBestFirstWithDyadRankedNodeQueueFactory extends HASCOViaFDAndBestFirstFactory<Double> {

	/**
	 * Constructs a new HASCO factory with a dyad ranked OPEN list configured with
	 * the given parameters.
	 *
	 * @param openConfig
	 */
	public HASCOViaFDAndBestFirstWithDyadRankedNodeQueueFactory(final IBestFirstQueueConfiguration<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, Double>, TFDNode, String, Double> openConfig) {
		super();
		this.setNodeEvaluator(n -> 1.0);
		this.setSearchFactory(new DyadRankedBestFirstFactory<>(openConfig));
	}

	@Override
	public void setNodeEvaluator(final IPathEvaluator<TFDNode, String, Double> nodeEvaluator) {
		this.setSearchProblemTransformer(new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<>(n -> {
			if (!(n instanceof BackPointerPath)) {
				throw new IllegalArgumentException("This variant of HASCO currently only works with back-pointer based nodes.");
			}
			if (((BackPointerPath<?, ?, ?>) n).isGoal()) {
				return nodeEvaluator.evaluate(n);
			} else {
				return 1.0;
			}
		}));
	}

	@Override
	public HASCOViaFDAndBestFirst<Double> getAlgorithm() {
		HASCOViaFDAndBestFirst<Double> hasco = super.getAlgorithm();
		hasco.setCreateComponentInstancesFromNodesInsteadOfPlans(true);
		return hasco;
	}
}
