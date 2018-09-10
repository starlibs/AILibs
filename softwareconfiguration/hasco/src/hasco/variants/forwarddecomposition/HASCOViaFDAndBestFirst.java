package hasco.variants.forwarddecomposition;


import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.probleminputs.GraphSearchProblemInput;

public class HASCOViaFDAndBestFirst<V extends Comparable<V>> extends HASCOViaFD<GeneralEvaluatedTraversalTree<TFDNode, String, V>, V> {
	
	public HASCOViaFDAndBestFirst(RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, AlgorithmProblemTransformer<GraphSearchProblemInput<TFDNode, String, V>, GeneralEvaluatedTraversalTree<TFDNode, String, V>> searchProblemTransformer) {
		super(configurationProblem, new BestFirstFactory<>(), searchProblemTransformer);
	}
}
