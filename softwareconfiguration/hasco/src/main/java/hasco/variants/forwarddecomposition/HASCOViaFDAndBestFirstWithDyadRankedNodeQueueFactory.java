package hasco.variants.forwarddecomposition;

import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.DyadRankedBestFirstFactory;
import jaicore.search.algorithms.standard.bestfirst.IBestFirstQueueConfiguration;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer;

public class HASCOViaFDAndBestFirstWithDyadRankedNodeQueueFactory extends HASCOViaFDAndBestFirstFactory<Double> {

	public HASCOViaFDAndBestFirstWithDyadRankedNodeQueueFactory(
			IBestFirstQueueConfiguration<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, Double>, TFDNode, String, Double> OPENConfig) {
		super();
		this.setNodeEvaluator(n->0.0);
		this.setSearchFactory(new DyadRankedBestFirstFactory<>(OPENConfig));
	}

	@Override
	public void setNodeEvaluator(INodeEvaluator<TFDNode, Double> nodeEvaluator) {
		System.err.println("Set node eval correctly");
		setSearchProblemTransformer(
				new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<>(n -> {
					if (n.isGoal()) {
						System.err.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA Evaluate node");
						return nodeEvaluator.f(n);
					} else {
						return 0.0;
					}
				}));
	}
}
