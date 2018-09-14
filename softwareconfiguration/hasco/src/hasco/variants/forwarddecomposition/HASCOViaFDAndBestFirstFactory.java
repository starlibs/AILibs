package hasco.variants.forwarddecomposition;

import hasco.core.DefaultHASCOPlanningGraphGeneratorDeriver;
import hasco.core.HASCOFactory;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionReducer;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;

public class HASCOViaFDAndBestFirstFactory<V extends Comparable<V>> extends HASCOFactory<GeneralEvaluatedTraversalTree<TFDNode, String, V>, TFDNode, String, V>{
	public HASCOViaFDAndBestFirstFactory() {
		setSearchFactory(new BestFirstFactory<>());
		setPlanningGraphGeneratorDeriver(new DefaultHASCOPlanningGraphGeneratorDeriver<>(new ForwardDecompositionReducer<>()));
	}
}
