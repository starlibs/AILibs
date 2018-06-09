package autofe.algorithm.hasco.filter.meta;

import weka.core.Instances;

public interface IFilter {
	public Instances applyFilter(final Instances inputData);
}
