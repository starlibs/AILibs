package autofe.algorithm.hasco.evaluation;

import autofe.algorithm.hasco.HASCOFE;
import autofe.util.DataSet;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.core.INodeEvaluator;

public abstract class AbstractHASCOFENodeEvaluator implements INodeEvaluator<TFDNode, Double> {
	private HASCOFE hascoFE;

	protected DataSet data;

	public void setData(final DataSet data) {
		this.data = data;
	}

	public HASCOFE getHascoFE() {
		return hascoFE;
	}

	public void setHascoFE(HASCOFE hascoFE) {
		this.hascoFE = hascoFE;
	}

}
