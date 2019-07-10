package ai.libs.jaicore.ml.core.predictivemodel;

import java.util.List;

import org.api4.java.ai.ml.AILabeledAttributeArrayDataset;
import org.api4.java.ai.ml.IDataset;
import org.api4.java.ai.ml.IInstance;

import ai.libs.jaicore.ml.core.exception.ConfigurationException;
import ai.libs.jaicore.ml.core.exception.PredictionException;

/**
 * The {@link IPredictiveModel} corresponds to a model which can be used to make
 * predictions based on given {@link IInstance}es.
 *
 * @author Alexander Hetzer, Julian Lienen
 *
 * @param <T>
 *            The type of the target that this {@link IPredictiveModel}
 *            predicts.
 * @param <I>
 *            The type of the instances stored in the data set specified by the generic parameter <D>.
 * @param <D>
 *            The type of the data set used to learn from and predict batches.
 */
public interface IPredictiveModel<T, I, D extends IDataset<I>> {

	/**
	 * Performs a prediction based on the given {@link IInstance} and returns the
	 * result.
	 *
	 * @param instance
	 *            The {@link IInstance} for which a prediction should be made.
	 * @return The result of the prediction.
	 * @throws PredictionException
	 *             If something fails during the prediction process.
	 */
	public T predict(I instance) throws PredictionException;

	/**
	 * Performs multiple predictions based on the {@link IInstance}s contained in
	 * the given {@link AILabeledAttributeArrayDataset}s and returns the result.
	 *
	 * @param dataset
	 *            The {@link AILabeledAttributeArrayDataset} for which predictions should be made.
	 * @return The result of the predictions.
	 * @throws PredictionException
	 *             If something fails during the prediction process.
	 */
	public List<T> predict(D dataset) throws PredictionException;

	/**
	 * Returns the {@link IPredictiveModelConfiguration} of this model.
	 *
	 * @return The {@link IPredictiveModelConfiguration} of this model.
	 */
	public IPredictiveModelConfiguration getConfiguration();

	/**
	 * Sets the {@link IPredictiveModelConfiguration} of this model to the given
	 * one.
	 *
	 * @throws ConfigurationException
	 *             If something fails during the configuration process.
	 */
	public void setConfiguration(IPredictiveModelConfiguration configuration) throws ConfigurationException;

}
