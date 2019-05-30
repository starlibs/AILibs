package jaicore.ml.tsc.pipeline;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.filter.IFilter;

/**
 * Class used within a pipeline that specified how to apply a filter on a
 * dataset.
 *
 * @author fischor
 */
public class FilterHandler {

	/** THe filter to apply. */
	private IFilter filter;

	/** The index of the value matrix in the dataset to apply the filter to. */
	private int valueMatrixIndex;

	/**
	 * Whether or not the transformed matrix shall be appended to the dataset after
	 * transform.
	 */
	private boolean append;

	public FilterHandler(final IFilter filter, final int valueMatrixIndex, final boolean append) {
		this.filter = filter;
		this.valueMatrixIndex = valueMatrixIndex;
		this.append = append;
	}

	public void fit(final TimeSeriesDataset dataset) {
		// Get values and fit filter.
		double[][] matrix = dataset.getValues(this.valueMatrixIndex);
		this.filter.fit(matrix);
	}

	public void transform(final TimeSeriesDataset dataset) {
		// Get the values and timestamps.
		double[][] valueMatrix = dataset.getValues(this.valueMatrixIndex);
		double[][] timestampMatrix = dataset.getTimestampsOrNull(this.valueMatrixIndex);
		// Transform the values.
		double[][] valueMatrixTransformed = this.filter.transform(valueMatrix);
		// Append (at the end) or replace.
		if (this.append) {
			dataset.add(valueMatrix, timestampMatrix);
		} else {
			dataset.replace(this.valueMatrixIndex, valueMatrixTransformed, timestampMatrix);
		}
	}
}