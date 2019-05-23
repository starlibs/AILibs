package autofe.algorithm.hasco.filter.meta;

import autofe.util.DataSet;

public interface IFilter extends Cloneable {

	public DataSet applyFilter(final DataSet inputData, final boolean copy) throws InterruptedException;

	public Object clone() throws CloneNotSupportedException;
}
