package jaicore.ml.tsc.pipeline;

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