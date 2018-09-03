package hasco.variants;

import hasco.core.HASCOFactory;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionReducer;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.problemtransformers.GraphSearchProblemInputToGeneralEvaluatedTraversalTreeViaRDFS;

public class HASCOViaFDAndBestFirstWithRandomCompletionsFactory extends HASCOFactory<GeneralEvaluatedTraversalTree<TFDNode, String, Double>, TFDNode, String, Double> {
	public HASCOViaFDAndBestFirstWithRandomCompletionsFactory() {
		setSearchFactory(new BestFirstFactory<>());
		setSearchProblemTransformer(new GraphSearchProblemInputToGeneralEvaluatedTraversalTreeViaRDFS<>(n -> null, 1, 3));
		setPlanningGraphGeneratorDeriver(new ForwardDecompositionReducer<>());
	}
}
