package hasco.variants.forwarddecomposition;

import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.DyadRankedBestFirstFactory;
import jaicore.search.algorithms.standard.bestfirst.IBestFirstQueueConfiguration;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer;

public class HASCOViaFDAndBestFirstWithDyadRankedNodeQueueFactory extends HASCOViaFDAndBestFirstFactory<Double> {
	public HASCOViaFDAndBestFirstWithDyadRankedNodeQueueFactory(IBestFirstQueueConfiguration<GraphSearchWithSubpathEvaluationsInput<TFDNode,String,Double>, TFDNode, String, Double> OPENConfig) {
		this.setSearchFactory(new DyadRankedBestFirstFactory<>(OPENConfig));
		this.setNodeEvaluator(null);
	}
	
	@Override
	public void setNodeEvaluator(INodeEvaluator<TFDNode, Double> nodeEvaluator) {
		setSearchProblemTransformer(new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<>(n -> 0.0));
	}
}
