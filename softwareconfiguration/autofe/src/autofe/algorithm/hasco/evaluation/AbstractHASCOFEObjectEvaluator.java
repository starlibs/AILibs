package autofe.algorithm.hasco.evaluation;


import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import jaicore.basic.IObjectEvaluator;

public abstract class AbstractHASCOFEObjectEvaluator implements IObjectEvaluator<FilterPipeline, Double> {
	protected DataSet data;

	public void setData(final DataSet data) {
		this.data = data;
	}
}
