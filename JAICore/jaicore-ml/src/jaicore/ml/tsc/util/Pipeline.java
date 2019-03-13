package jaicore.ml.tsc.util;

import java.util.List;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ASimplifiedTSClassifier;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.filter.IFilter;

/**
 * Pipeline
 * 
 * @param <IFiler>
 */
public class Pipeline<TARGETDOMAIN> {

    /**
     * Class used within a pipeline that specified how to apply a filter on a
     * dataset.
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

    public List<FilterHandler> filter;
    public ASimplifiedTSClassifier<TARGETDOMAIN> classifier;

    public void train(TimeSeriesDataset dataset) {

    }

    public TARGETDOMAIN predict(final double[] univInstance) throws PredictionException {
        return this.classifier.predict(univInstance);
    };

    public TARGETDOMAIN predict(final double[] univInstance, final double[] timestamps) throws PredictionException {
        return predict(univInstance);
    }

    public TARGETDOMAIN predict(final List<double[]> multivInstance) throws PredictionException {
        return this.classifier.predict(multivInstance);
    };

    // TODO: Just a single timestamp.
    public TARGETDOMAIN predict(final List<double[]> multivInstance, final List<double[]> timestamps)
            throws PredictionException {
        return this.classifier.predict(multivInstance, timestamps);
    }

    public List<TARGETDOMAIN> predict(final TimeSeriesDataset dataset) throws PredictionException {
        return predict(dataset);
    };

}