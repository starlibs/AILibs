package jaicore.ml.tsc.pipeline;

import java.util.List;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ASimplifiedTSClassifier;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;

/**
 * Pipeline for a time series classification task.
 * 
 * A pipeline consists of a list of filters and a classifier. When given a
 * datset the pipeline applies each filters in order to a specific value matrix
 * of the dataset. After each filter is applied, the fitlered dataset is given
 * to the classifier. This procedure applies to the train and test phase
 * equally.
 * 
 * @param TARGETDOMAIN The type of the targets for the classification task.
 * @author fischor
 */
public class Pipeline<TARGETDOMAIN> {

    public List<FilterHandler> filter;
    public ASimplifiedTSClassifier<TARGETDOMAIN> classifier;

    public void train(TimeSeriesDataset dataset) {
        // TODO
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

    public TARGETDOMAIN predict(final List<double[]> multivInstance, final List<double[]> timestamps)
            throws PredictionException {
        return this.classifier.predict(multivInstance, timestamps);
    }

    public List<TARGETDOMAIN> predict(final TimeSeriesDataset dataset) throws PredictionException {
        return predict(dataset);
    };

}