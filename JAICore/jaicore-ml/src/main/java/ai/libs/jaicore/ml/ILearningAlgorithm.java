package ai.libs.jaicore.ml;

import ai.libs.jaicore.ml.classification.multiclass.LearningAlgorithmConfigurationFailedException;
import ai.libs.jaicore.ml.core.exception.PredictionException;
import ai.libs.jaicore.ml.core.exception.TrainingException;

/**
 * This interface defines a general interface for learning algorithms that can fit data and, based on the internal model, may be used to make predictions on new data points.
 *
 * @author mwever
 *
 * @param <X> The type of instances.
 * @param <Y> The type of target attributes.
 * @param <C> The type of the configuration.
 */
public interface ILearningAlgorithm<X, Y, C> {

	public void setConfig(C config) throws LearningAlgorithmConfigurationFailedException, InterruptedException;

	public void fit(X[] x, Y[] y) throws TrainingException, InterruptedException;

	public Y predict(X x) throws PredictionException, InterruptedException;

	public Y[] predict(X[] x) throws PredictionException, InterruptedException;

	public Y fitAndPredict(X[] xTrain, Y[] yTrain, X xPredict) throws TrainingException, PredictionException, InterruptedException;

	public Y[] fitAndPredict(X[] xTrain, Y[] yTrain, X[] xPredict) throws TrainingException, PredictionException, InterruptedException;

}
