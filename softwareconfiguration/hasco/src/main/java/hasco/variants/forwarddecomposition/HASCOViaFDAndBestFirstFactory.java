package hasco.variants.forwarddecomposition;

import hasco.core.DefaultHASCOPlanningGraphGeneratorDeriver;
import hasco.core.HASCO;
import hasco.core.HASCOFactory;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.ForwardDecompositionReducer;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.StandardBestFirstFactory;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer;

/**
 * This factory makes it easier to create HASCO objects.
 * In contrast to the standard HASCOFactory, it is only necessary to set the problem and a node evaluator
 * 
 * Note that the standard HASCO search problem is a GraphSearchProblem, but BestFirst needs sub-path evaluation, so providing such a transformation is a MUST.
 * 
 * It is possible to set the node evaluator, which will be then used in the search
 * 
 * @author fmohr
 *
 * @param <V>
 */
public class HASCOViaFDAndBestFirstFactory<V extends Comparable<V>> extends HASCOFactory<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, TFDNode, String, V> {
	
	public HASCOViaFDAndBestFirstFactory() {
		super();
		setPlanningGraphGeneratorDeriver(new DefaultHASCOPlanningGraphGeneratorDeriver<>(new ForwardDecompositionReducer<>()));
		setSearchFactory(new StandardBestFirstFactory<>());
	}
	
	public HASCOViaFDAndBestFirstFactory(INodeEvaluator<TFDNode, V> nodeEvaluator) {
		this();
		setNodeEvaluator(nodeEvaluator);
	}
	
	public void setNodeEvaluator(INodeEvaluator<TFDNode, V> nodeEvaluator) {
		setSearchProblemTransformer(new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<>(nodeEvaluator));
	}
	
	public StandardBestFirstFactory<TFDNode, String, V> getSearchFactory() {
		return (StandardBestFirstFactory<TFDNode, String, V>)super.getSearchFactory();
	}
	
	@Override
	public HASCO<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, TFDNode, String, V> getAlgorithm() {
		if (getSearchProblemTransformer() == null)
			throw new IllegalStateException("Cannot create HASCO with BestFirst, because no node evaluator has been set. Please set a node evaluator prior to invoking getAlgorithm()");
		return super.getAlgorithm();
	}
}
