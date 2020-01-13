package ai.libs.jaicore.ml.core.learner;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.exception.LearnerConfigurationFailedException;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

public abstract class ASupervisedLearner<I extends ILabeledInstance, D extends ILabeledDataset<? extends I>, P extends IPrediction, B extends IPredictionBatch> implements ISupervisedLearner<I, D> {

	private Map<String, Object> config;

	protected ASupervisedLearner(final Map<String, Object> config) {
		this.config = config;
	}

	protected ASupervisedLearner() {
		this.config = new HashMap<>();
	}

	@Override
	public P fitAndPredict(final D dTrain, final I xTest) throws TrainingException, PredictionException, InterruptedException {
		this.fit(dTrain);
		return this.predict(xTest);
	}

	@Override
	public B fitAndPredict(final D dTrain, final I[] xTest) throws TrainingException, PredictionException, InterruptedException {
		this.fit(dTrain);
		return this.predict(xTest);
	}

	@Override
	public B fitAndPredict(final D dTrain, final D dTest) throws TrainingException, PredictionException, InterruptedException {
		this.fit(dTrain);
		return this.predict(dTest);
	}

	@SuppressWarnings("unchecked")
	@Override
	public B predict(final D dTest) throws PredictionException, InterruptedException {
		Class<I> clazz = (Class<I>) dTest.iterator().next().getClass();
		I[] instancesAsArray = (I[]) Array.newInstance(clazz, dTest.size());
		for (int i = 0; i < dTest.size(); i++) {
			instancesAsArray[i] = dTest.get(i);
		}
		return this.predict(instancesAsArray);
	}

	@Override
	public abstract P predict(I xTest) throws PredictionException, InterruptedException;

	@Override
	public abstract B predict(I[] dTest) throws PredictionException, InterruptedException;

	@Override
	public void setConfig(final Map<String, Object> config) throws LearnerConfigurationFailedException, InterruptedException {
		this.config = config;
	}

	@Override
	public Map<String, Object> getConfig() {
		return this.config;
	}
}
