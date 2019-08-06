package ai.libs.jaicore.ml.classification;

import java.util.Arrays;

import org.api4.java.ai.ml.dataset.IFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;
import org.api4.java.ai.ml.learner.ISupervisedLearner;
import org.api4.java.ai.ml.learner.LearnerConfigurationFailedException;
import org.api4.java.ai.ml.learner.fit.TrainingException;
import org.api4.java.ai.ml.learner.predict.IPrediction;
import org.api4.java.ai.ml.learner.predict.IPredictionBatch;
import org.api4.java.ai.ml.learner.predict.PredictionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.dataset.Prediction;

public abstract class ASupervisedLearner<C, X, Y, I extends IFeatureInstance<X> & ILabeledInstance<Y>, D extends ISupervisedDataset<X, Y, I>> implements ISupervisedLearner<C, X, Y, I, D> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ASupervisedLearner.class);
	private C config;

	@Override
	public IPrediction<Y> fitAndPredict(final D dTrain, final I xTest) throws TrainingException, PredictionException, InterruptedException {
		this.fit(dTrain);
		return this.predict(xTest);
	}

	@Override
	public IPredictionBatch<Y> fitAndPredict(final D dTrain, final I[] xTest) throws TrainingException, PredictionException, InterruptedException {
		this.fit(dTrain);
		return this.predict(xTest);
	}

	@Override
	public IPredictionBatch<Y> fitAndPredict(final D dTrain, final D dTest) throws TrainingException, PredictionException, InterruptedException {
		this.fit(dTrain);
		return this.predict(dTest);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IPredictionBatch<Y> predict(final D dTest) throws PredictionException, InterruptedException {
		return this.predict((I[]) dTest.stream().toArray());
	}

	@SuppressWarnings("unchecked")
	@Override
	public IPredictionBatch<Y> predict(final I[] dTest) throws PredictionException, InterruptedException {
		return new PredictionBatch<>((IPrediction<Y>[]) Arrays.stream(dTest).map(x -> {
			try {
				return this.predict(x);
			} catch (PredictionException e) {
				LOGGER.error("Could not predict instance {}", x, e);
				return new Prediction<>(Double.NaN);
			} catch (InterruptedException e) {
				LOGGER.error("Prediction has been interrupted.", e);
				Thread.currentThread().interrupt();
				return new Prediction<>(Double.NaN);
			}
		}).toArray());
	}

	@Override
	public void setConfig(final C config) throws LearnerConfigurationFailedException, InterruptedException {
		this.config = config;
	}

	public C getConfig() {
		return this.config;
	}
}
