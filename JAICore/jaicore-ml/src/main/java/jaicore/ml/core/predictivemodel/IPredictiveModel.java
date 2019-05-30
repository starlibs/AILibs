package jaicore.ml.core.predictivemodel;

import java.util.List;

import jaicore.ml.core.dataset.AILabeledAttributeArrayDataset;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.exception.ConfigurationException;
import jaicore.ml.core.exception.PredictionException;

/**
 * The {@link IPredictiveModel} corresponds to a model which can be used to make
 * predictions based on given {@link IInstance}es.
 * 
 * @author Alexander Hetzer, Julian Lienen
 *
 * @param <TARGET>
 *            The type of the target that this {@link IPredictiveModel}
 *            predicts.
 * @param <INSTANCE>
 *            The type of the instances stored in the data set specified by the
 *            generic parameter <DATASET>.
 * @param <DATASET>
 *            The type of the data set used to learn from and predict batches.
 */
public interface IPredictiveModel<TARGET, INSTANCE, DATASET extends IDataset<INSTANCE>> {

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
	public TARGET predict(INSTANCE instance) throws PredictionException;

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
	public List<TARGET> predict(DATASET dataset) throws PredictionException;

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
