package hasco.variants.forwarddecomposition;


import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class HASCOViaFDAndBestFirst<V extends Comparable<V>> extends HASCOViaFD<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, V> {
	
	public HASCOViaFDAndBestFirst(RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, AlgorithmProblemTransformer<GraphSearchWithPathEvaluationsInput<TFDNode, String, V>, GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>> searchProblemTransformer) {
		super(configurationProblem, new BestFirstFactory<>(), searchProblemTransformer);
	}
}
