package autofe.algorithm.hasco.evaluation;

import java.util.Collection;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import jaicore.basic.IObjectEvaluator;

public abstract class AbstractHASCOFEObjectEvaluator<T> implements IObjectEvaluator<FilterPipeline<T>, Double> {
	protected Collection<T> data;

	public void setData(final Collection<T> data) {
		this.data = data;
	}
}
