package ai.libs.jaicore.ml.core.learner;

import java.util.Arrays;
import java.util.Map;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.LearnerConfigurationFailedException;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.ai.ml.core.learner.algorithm.IPrediction;
import org.api4.java.ai.ml.core.learner.algorithm.IPredictionBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.core.evaluation.Prediction;
import ai.libs.jaicore.ml.core.evaluation.PredictionBatch;

public abstract class ASupervisedLearner<I extends ILabeledInstance, D extends ILabeledDataset<I>> implements ISupervisedLearner<I, D> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ASupervisedLearner.class);
	private Map<String, Object> config;

	@Override
	public IPrediction fitAndPredict(final D dTrain, final I xTest) throws TrainingException, PredictionException, InterruptedException {
		this.fit(dTrain);
		return this.predict(xTest);
	}

	@Override
	public IPredictionBatch fitAndPredict(final D dTrain, final I[] xTest) throws TrainingException, PredictionException, InterruptedException {
		this.fit(dTrain);
		return this.predict(xTest);
	}

	@Override
	public IPredictionBatch fitAndPredict(final D dTrain, final D dTest) throws TrainingException, PredictionException, InterruptedException {
		this.fit(dTrain);
		return this.predict(dTest);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IPredictionBatch predict(final D dTest) throws PredictionException, InterruptedException {
		return this.predict((I[]) dTest.stream().toArray());
	}

	@Override
	public IPredictionBatch predict(final I[] dTest) throws PredictionException, InterruptedException {
		return new PredictionBatch((IPrediction[]) Arrays.stream(dTest).map(x -> {
			try {
				return this.predict(x);
			} catch (PredictionException e) {
				LOGGER.error("Could not predict instance {}", x, e);
				return new Prediction(Double.NaN);
			} catch (InterruptedException e) {
				LOGGER.error("Prediction has been interrupted.", e);
				Thread.currentThread().interrupt();
				return new Prediction(Double.NaN);
			}
		}).toArray());
	}

	@Override
	public void setConfig(final Map<String, Object> config) throws LearnerConfigurationFailedException, InterruptedException {
		this.config = config;
	}

	@Override
	public Map<String, Object> getConfig() {
		return this.config;
	}
}
