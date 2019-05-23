package autofe.algorithm.hasco.evaluation;

import autofe.util.DataSet;

public abstract class AbstractHASCOFEEvaluator {

	public static final double MAX_EVAL_VALUE = 20000d;

	protected DataSet data;

	protected static final double ATT_COUNT_PENALTY = 1;

	public void setData(final DataSet data) {
		this.data = data;
	}

}
