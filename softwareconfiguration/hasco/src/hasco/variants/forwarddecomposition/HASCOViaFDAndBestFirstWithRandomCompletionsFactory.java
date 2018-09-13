package hasco.variants.forwarddecomposition;

import hasco.core.DefaultHASCOPlanningGraphGeneratorDeriver;
import hasco.core.HASCOFactory;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionReducer;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.problemtransformers.GraphSearchProblemInputToGeneralEvaluatedTraversalTreeViaRDFS;

public class HASCOViaFDAndBestFirstWithRandomCompletionsFactory extends HASCOFactory<GeneralEvaluatedTraversalTree<TFDNode, String, Double>, TFDNode, String, Double> {
	public HASCOViaFDAndBestFirstWithRandomCompletionsFactory() {
		setSearchFactory(new BestFirstFactory<>());
		setSearchProblemTransformer(new GraphSearchProblemInputToGeneralEvaluatedTraversalTreeViaRDFS<>(n -> null, new DefaultPathPriorizingPredicate<>(), 1, 3, -1, -1));
		setPlanningGraphGeneratorDeriver(new DefaultHASCOPlanningGraphGeneratorDeriver<>(new ForwardDecompositionReducer<>()));
	}
}
