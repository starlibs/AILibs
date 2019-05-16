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
    IFilter filter;

    /** The index of the value matrix in the dataset to apply the filter to. */
    int valueMatrixIndex;

    /**
     * Whether or not the transformed matrix shall be appended to the dataset after
     * transform.
     */
    boolean append;

    public FilterHandler(IFilter filter, int valueMatrixIndex, boolean append) {
        this.filter = filter;
        this.valueMatrixIndex = valueMatrixIndex;
        this.append = append;
    }

    public void fit(TimeSeriesDataset dataset) {
        // Get values and fit filter.
        double[][] matrix = dataset.getValues(valueMatrixIndex);
        this.filter.fit(matrix);
    }

    public void transform(TimeSeriesDataset dataset) {
        // Get the values and timestamps.
        double[][] valueMatrix = dataset.getValues(valueMatrixIndex);
        double[][] timestampMatrix = dataset.getTimestampsOrNull(valueMatrixIndex);
        // Transform the values.
        double[][] valueMatrixTransformed = this.filter.transform(valueMatrix);
        // Append (at the end) or replace.
        if (this.append)
            dataset.add(valueMatrix, timestampMatrix);
        else
            dataset.replace(valueMatrixIndex, valueMatrixTransformed, timestampMatrix);
    }
}