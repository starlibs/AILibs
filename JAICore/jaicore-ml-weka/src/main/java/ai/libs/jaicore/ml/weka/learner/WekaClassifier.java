package ai.libs.jaicore.ml.weka.learner;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.serialization.UnsupportedAttributeTypeException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.LearnerConfigurationFailedException;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassification;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassificationPredictionBatch;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;
import ai.libs.jaicore.ml.weka.dataset.WekaInstance;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.OptionHandler;

public class WekaClassifier extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<ILabeledInstance>, ISingleLabelClassification, ISingleLabelClassificationPredictionBatch> implements IWekaClassifier {
	private static final Logger LOGGER = LoggerFactory.getLogger(WekaClassifier.class);

	private final String name;
	private String[] options;
	private Classifier wrappedClassifier;

	private ILabeledInstanceSchema schema;

	public WekaClassifier(final String name, final String[] options) {
		this.name = name;
		this.options = options;
		try {
			this.wrappedClassifier = AbstractClassifier.forName(name, options);
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not find classifier for name " + name + " or could not set its options to " + Arrays.toString(options), e);
		}
	}

	public WekaClassifier(final Classifier classifier) {
		this.wrappedClassifier = classifier;
		this.name = classifier.getClass().getName();
	}

	public String getName() {
		return this.name;
	}

	public String[] getOptions() {
		return this.options;
	}

	@Override
	public void fit(final ILabeledDataset<ILabeledInstance> dTrain) throws TrainingException, InterruptedException {
		WekaInstances data = new WekaInstances(dTrain);

		try {
			this.wrappedClassifier.buildClassifier(data.getInstances());
		} catch (Exception e) {
			throw new TrainingException("Could not build " + this.getClass().getSimpleName() + " due to exception", e);
		}

	}

	@Override
	public ISingleLabelClassification predict(final ILabeledInstance xTest) throws PredictionException, InterruptedException {
		WekaInstance instance;
		if (xTest instanceof WekaInstance) {
			instance = (WekaInstance) xTest;
		} else {
			try {
				instance = new WekaInstance(this.schema, xTest);
			} catch (UnsupportedAttributeTypeException e) {
				throw new PredictionException("Could not create WekaInstance object from given instance.");
			}
		}

		try {
			return new SingleLabelClassification((int) this.wrappedClassifier.classifyInstance(instance.getElement()));
		} catch (Exception e) {
			throw new PredictionException("Could not make a prediction since an exception occurred in the wrapped weka classifier.", e);
		}
	}

	@Override
	public ISingleLabelClassificationPredictionBatch predict(final ILabeledDataset<ILabeledInstance> dTest) throws PredictionException, InterruptedException {
		return this.predict((ILabeledInstance[]) dTest.stream().toArray());
	}

	@Override
	public ISingleLabelClassificationPredictionBatch predict(final ILabeledInstance[] dTest) throws PredictionException, InterruptedException {
		return new SingleLabelClassificationPredictionBatch(Arrays.stream(dTest).map(x -> {
			try {
				return this.predict(x);
			} catch (PredictionException e) {
				LOGGER.error("There was an issue while making a prediction", e);
				return null;
			} catch (InterruptedException e) {
				LOGGER.error("Got interrupted while predicting. ", e);
				Thread.currentThread().interrupt();
				return null;
			}
		}).collect(Collectors.toList()));
	}

	@Override
	public void setConfig(final Map<String, Object> config) throws LearnerConfigurationFailedException, InterruptedException {
		try {
			((OptionHandler) this.wrappedClassifier).setOptions(this.options);
		} catch (Exception e) {
			throw new LearnerConfigurationFailedException("Could not set config for " + WekaClassifier.class.getSimpleName());
		}
	}

	@Override
	public Classifier getClassifier() {
		return this.wrappedClassifier;
	}

}
