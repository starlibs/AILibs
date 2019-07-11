package ai.libs.hasco.variants.forwarddecomposition;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.StandardBestFirstFactory;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer;

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
public class HASCOViaFDAndBestFirstFactory<V extends Comparable<V>> extends HASCOViaFDFactory<GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>, V> {

	public HASCOViaFDAndBestFirstFactory() {
		super();
		this.setSearchFactory(new StandardBestFirstFactory<>());
	}

	public HASCOViaFDAndBestFirstFactory(final IPathEvaluator<TFDNode, String, V> nodeEvaluator) {
		this();
		this.setNodeEvaluator(nodeEvaluator);
	}

	public void setNodeEvaluator(final IPathEvaluator<TFDNode, String, V> nodeEvaluator) {
		this.setSearchProblemTransformer(new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<>(nodeEvaluator));
	}

	@Override
	public StandardBestFirstFactory<TFDNode, String, V> getSearchFactory() {
		return (StandardBestFirstFactory<TFDNode, String, V>)super.getSearchFactory();
	}

	@Override
	@SuppressWarnings("unchecked")
	public GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<TFDNode, String, V> getSearchProblemTransformer() {
		return (GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<TFDNode, String, V>)super.getSearchProblemTransformer();
	}

	@Override
	public HASCOViaFDAndBestFirst<V> getAlgorithm() {
		if (this.getSearchProblemTransformer() == null) {
			throw new IllegalStateException("Cannot create HASCO with BestFirst, because no node evaluator has been set. Please set a node evaluator prior to invoking getAlgorithm()");
		}
		return new HASCOViaFDAndBestFirst<>(super.getAlgorithm());
	}
}
