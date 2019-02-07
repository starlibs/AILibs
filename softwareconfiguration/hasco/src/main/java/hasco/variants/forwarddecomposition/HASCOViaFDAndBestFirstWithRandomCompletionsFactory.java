package hasco.variants.forwarddecomposition;

import java.util.function.Predicate;

import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS;

public class HASCOViaFDAndBestFirstWithRandomCompletionsFactory extends HASCOViaFDAndBestFirstFactory<Double> {
	
	private Predicate<TFDNode> priorizingPredicate;

	public HASCOViaFDAndBestFirstWithRandomCompletionsFactory() {
		super();
		setSearchProblemTransformer(new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<>(n -> null, priorizingPredicate, 1, 3, -1, -1));
	}

	public Predicate<TFDNode> getPriorizingPredicate() {
		return priorizingPredicate;
	}

	public void setPriorizingPredicate(Predicate<TFDNode> priorizingPredicate) {
		this.priorizingPredicate = priorizingPredicate;
	}
}
