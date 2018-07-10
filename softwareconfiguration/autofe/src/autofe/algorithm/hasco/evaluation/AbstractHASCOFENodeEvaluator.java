package autofe.algorithm.hasco.evaluation;

import autofe.algorithm.hasco.HASCOFE;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import hasco.core.Util;
import hasco.model.ComponentInstance;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.Node;

public abstract class AbstractHASCOFENodeEvaluator extends AbstractHASCOFEEvaluator
		implements INodeEvaluator<TFDNode, Double> {

	protected static final double MAX_EVAL_VALUE = 20000d;

	private HASCOFE hascoFE;

	// Maximum size of a pipeline
	protected int maxPipelineSize;

	public AbstractHASCOFENodeEvaluator(final int maxPipelineSize) {
		this.maxPipelineSize = maxPipelineSize;
	}

	public HASCOFE getHascoFE() {
		return hascoFE;
	}

	public void setHascoFE(HASCOFE hascoFE) {
		this.hascoFE = hascoFE;
	}

	public FilterPipeline getPipelineFromNode(final Node<TFDNode, ?> node) throws Exception {
		ComponentInstance ci = Util.getSolutionCompositionFromState(this.getHascoFE().getHasco().getComponents(),
				node.getPoint().getState());
		if (ci == null)
			return null;

		return this.getHascoFE().getHasco().getFactory().getComponentInstantiation(ci);
	}
}
