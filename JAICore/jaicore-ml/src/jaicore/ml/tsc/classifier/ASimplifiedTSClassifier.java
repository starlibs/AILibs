package jaicore.ml.tsc.classifier;

import java.util.List;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;

/**
 * Simplified batch-learning time series classifier which can be trained and
 * used as a predictor. Uses <code>algorithm</code> to train the model
 * parameters (if necessary).
 * 
 * @author Julian Lienen
 * 
 */
public abstract class ASimplifiedTSClassifier<TARGETDOMAIN> {
	/**
	 * The algorithm object used for the training of the classifier.
	 */
	protected ASimplifiedTSCAlgorithm<TARGETDOMAIN, ? extends ASimplifiedTSClassifier<TARGETDOMAIN>> algorithm;

	/**
	 * Constructor for a simplified time series classifier.
	 * 
	 * @param algorithm
	 *            The algorithm object used for the training of the classifier
	 */
	public ASimplifiedTSClassifier(
			ASimplifiedTSCAlgorithm<TARGETDOMAIN, ? extends ASimplifiedTSClassifier<TARGETDOMAIN>> algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * Performs a prediction based on the given univariate double[] instance
	 * representation and returns the result.
	 * 
	 * @param univInstance
	 *            Univariate instance given by a double vector of time series values
	 *            used for the prediction
	 * @return Returns the result of the prediction
	 * @throws PredictionException
	 *             If something fails during the prediction process.
	 */
	public abstract TARGETDOMAIN predict(final double[] univInstance) throws PredictionException;

	/**
	 * Performs a prediction based on the given multivariate list of double[]
	 * instance representation and returns the result.
	 * 
	 * @param multivInstance
	 *            Multivariate instance given by a list of multiple double[] time
	 *            series used for the prediction
	 * @return Returns the result of the prediction
	 * @throws PredictionException
	 *             If something fails during the prediction process.
	 */
	public abstract TARGETDOMAIN predict(final List<double[]> multivInstance) throws PredictionException;

	/**
	 * Performs predictions based on the given instances in the given dataset.
	 * 
	 * @param dataset
	 *            The {@link TimeSeriesDataset} for which predictions should be
	 *            made.
	 * @return Returns the result of the predictions
	 * @throws PredictionException
	 *             If something fails during the prediction process
	 */
	public abstract List<TARGETDOMAIN> predict(final TimeSeriesDataset dataset) throws PredictionException;

	/**
	 * Trains the simplified time series classifier model using the given
	 * {@link TimeSeriesDataset}.
	 * 
	 * @param dataset
	 *            The {@link TimeSeriesDataset} which should be used for the
	 *            training.
	 * @throws TrainingException
	 *             If something fails during the training process.
	 */
	public abstract void train(final TimeSeriesDataset dataset) throws TrainingException;
}
