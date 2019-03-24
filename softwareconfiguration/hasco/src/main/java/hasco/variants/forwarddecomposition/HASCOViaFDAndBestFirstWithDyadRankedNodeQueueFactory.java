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
		this.setNodeEvaluator(n -> 1.0);
		this.setSearchFactory(new DyadRankedBestFirstFactory<>(OPENConfig));
	}

	@Override
	public void setNodeEvaluator(INodeEvaluator<TFDNode, Double> nodeEvaluator) {
		setSearchProblemTransformer(
				new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<>(n -> {
					if (n.isGoal()) {
						double f = nodeEvaluator.f(n); 
						System.err.println("Returning " + f + " for goal node.");
						return f;
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
