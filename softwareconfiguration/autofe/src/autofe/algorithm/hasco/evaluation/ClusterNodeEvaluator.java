package autofe.algorithm.hasco.evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.EvaluationUtils;
import hasco.core.Util;
import hasco.model.ComponentInstance;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.structure.core.Node;

/**
 * Evaluator used for node evaluation to guide the search using a simple
 * clustering benchmark function.
 * 
 * @author Julian Lienen
 *
 */
public class ClusterNodeEvaluator extends AbstractHASCOFENodeEvaluator {

	// Maximum size of a pipeline
	private int maxPipelineSize;

	public ClusterNodeEvaluator(final int maxPipelineDepth) {
		super();
		this.maxPipelineSize = maxPipelineDepth;
	}

	private static final Logger logger = LoggerFactory.getLogger(ClusterNodeEvaluator.class);

	@Override
	public Double f(Node<TFDNode, ?> node) throws Throwable {

		if (this.getHascoFE() == null)
			throw new IllegalStateException("HascoFE property of the cluster node evaluator must be initialized!");

		if (node.getParent() == null)
			return null;

		// If pipeline is too deep, assign worst value
		if (node.path().size() > this.maxPipelineSize)
			return 1.0;

		ComponentInstance ci = Util.getSolutionCompositionFromState(this.getHascoFE().getHasco().getComponents(),
				node.getPoint().getState());
		if (ci == null)
			return null;

		FilterPipeline pipe = this.getHascoFE().getHasco().getFactory().getComponentInstantiation(ci);
		if (pipe != null && pipe.getFilters() != null) {
			try {
				return EvaluationUtils.performClustering(pipe, this.data);
			} catch (Exception e) {
				e.printStackTrace();
				logger.warn("Could not evaluate pipeline. Reason: " + e.getMessage());
				return null;
			}
		} else {
			logger.debug("Null pipe");
			return null;
		}
	}
}
