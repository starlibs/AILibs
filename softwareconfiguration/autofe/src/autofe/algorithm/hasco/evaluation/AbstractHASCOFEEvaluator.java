package autofe.algorithm.hasco.evaluation;

import autofe.util.DataSet;

public abstract class AbstractHASCOFEEvaluator {

	protected DataSet data;

	protected static final double ATT_COUNT_PENALTY = 1;

	public void setData(final DataSet data) {
		this.data = data;
	}

}
