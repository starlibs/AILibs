package hasco.variants.forwarddecomposition;

import java.util.function.Predicate;

import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS;

public class HASCOViaFDAndBestFirstWithRandomCompletionsFactory extends HASCOViaFDAndBestFirstFactory<Double> {
	
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator = n -> null;
	private Predicate<TFDNode> priorizingPredicate;

	public HASCOViaFDAndBestFirstWithRandomCompletionsFactory() {
		super();
	}

	public Predicate<TFDNode> getPriorizingPredicate() {
		return priorizingPredicate;
	}

	public void setPriorizingPredicate(Predicate<TFDNode> priorizingPredicate) {
		this.priorizingPredicate = priorizingPredicate;
	}

	public INodeEvaluator<TFDNode, Double> getPreferredNodeEvaluator() {
		return preferredNodeEvaluator;
	}

	public void setPreferredNodeEvaluator(INodeEvaluator<TFDNode, Double> preferredNodeEvaluator) {
		this.preferredNodeEvaluator = preferredNodeEvaluator;
	}
	
	@Override
	public HASCOViaFDAndBestFirst<Double> getAlgorithm() {
		setSearchProblemTransformer(new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<>(preferredNodeEvaluator, priorizingPredicate, 1, 3, -1, -1));
		return super.getAlgorithm();
	}
}
