package ai.libs.jaicore.ml.classification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.dataset.attribute.IAttributeType;
import org.api4.java.ai.ml.dataset.attribute.nominal.INominalAttributeType;
import org.api4.java.ai.ml.dataset.supervised.INumericFeatureSupervisedInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;
import org.api4.java.ai.ml.learner.LearnerConfigurationFailedException;
import org.api4.java.ai.ml.learner.fit.TrainingException;
import org.api4.java.ai.ml.learner.predict.IPrediction;
import org.api4.java.ai.ml.learner.predict.IPredictionBatch;
import org.api4.java.ai.ml.learner.predict.PredictionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.dataset.Prediction;
import weka.classifiers.AbstractClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class WekaClassifier extends ASupervisedLearner<IWekaClassifierConfig, Double, Double, INumericFeatureSupervisedInstance<Double>, ISupervisedDataset<Double, Double, INumericFeatureSupervisedInstance<Double>>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(WekaClassifier.class);

	private final String name;
	private String[] options;
	private AbstractClassifier wrappedClassifier;
	private Instances metaData;
	private Map<Double, String> targetValueToClass = new HashMap<>();

	public WekaClassifier(final String name, final String[] options) {
		this.name = name;
		this.options = options;
		try {
			this.wrappedClassifier = (AbstractClassifier) AbstractClassifier.forName(name, options);
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not find classifier for name " + name + " or could not set its options to " + Arrays.toString(options), e);
		}
	}

	public String getName() {
		return this.name;
	}

	public String[] getOptions() {
		return this.options;
	}

	private Instances extractMetaData(final ISupervisedDataset<Double, Double, INumericFeatureSupervisedInstance<Double>> dTrain) {
		ArrayList<Attribute> attInfo = new ArrayList<>();
		for (int i = 0; i < dTrain.getFeatureTypes().size(); i++) {
			IAttributeType type = dTrain.getFeatureTypes().get(i);
			if (type instanceof INominalAttributeType) {
				attInfo.add(new Attribute("a" + i, ((INominalAttributeType) type).getValues()));
			}
		}

		if (dTrain.getLabelTypes().isEmpty() || dTrain.getLabelTypes().size() > 1) {
			throw new UnsupportedOperationException("No target resp. multiple targets are not supported.");
		}

		IAttributeType labelType = dTrain.getLabelTypes().get(0);
		if (labelType instanceof INominalAttributeType) {
			INominalAttributeType nomLabel = (INominalAttributeType) labelType;
			attInfo.add(new Attribute("class", new LinkedList<>(nomLabel.getValues())));
			this.targetValueToClass.clear();
			for (String value : ((INominalAttributeType) labelType).getValues()) {
				this.targetValueToClass.put(nomLabel.encodeToDouble(value), value);
			}
		} else {
			attInfo.add(new Attribute("class"));
		}

		Instances extractedMetaData = new Instances("Wrapped WEKA Instances", attInfo, 0);
		extractedMetaData.setClassIndex(extractedMetaData.numAttributes() - 1);
		return extractedMetaData;
	}

	@Override
	public void fit(final ISupervisedDataset<Double, Double, INumericFeatureSupervisedInstance<Double>> dTrain) throws TrainingException, InterruptedException {
		// extract the dataset's meta data and prepare the instances.
		this.metaData = this.extractMetaData(dTrain);

		Instances trainData = new Instances(this.metaData, 0);
		for (int i = 0; i < dTrain.size(); i++) {
			Instance newI = new DenseInstance(trainData.numAttributes());
			newI.setDataset(trainData);
			for (int j = 0; j < dTrain.get(i).getNumFeatures(); j++) {
				if (dTrain.getFeatureTypes().get(j) instanceof INominalAttributeType) {
					newI.setValue(j, ((INominalAttributeType) dTrain.getFeatureTypes().get(j)).decodeToString(dTrain.get(i).get(j)));
				} else {
					newI.setValue(j, dTrain.get(i).get(j));
				}
			}

			newI.setValue(newI.numAttributes() - 1, this.targetValueToClass.get(dTrain.get(i).getLabel()));
			trainData.add(newI);
		}
		try {
			this.wrappedClassifier.buildClassifier(trainData);
		} catch (Exception e) {
			throw new TrainingException("Could not build " + this.getClass().getSimpleName() + " due to exception", e);
		}

	}

	@Override
	public IPrediction<Double> predict(final INumericFeatureSupervisedInstance<Double> xTest) throws PredictionException, InterruptedException {
		Instance testInstance = new DenseInstance(this.metaData.numAttributes());
		testInstance.setDataset(this.metaData);
		IntStream.range(0, xTest.getNumFeatures()).forEach(ix -> testInstance.setValue(ix, xTest.get(ix)));
		try {
			return new Prediction<>(this.wrappedClassifier.classifyInstance(testInstance));
		} catch (Exception e) {
			throw new PredictionException("Could not make a prediction since an exception occurred in the wrapped weka classifier.", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public IPredictionBatch<Double> predict(final ISupervisedDataset<Double, Double, INumericFeatureSupervisedInstance<Double>> dTest) throws PredictionException, InterruptedException {
		return this.predict((INumericFeatureSupervisedInstance<Double>[]) dTest.stream().toArray());
	}

	@Override
	public IPredictionBatch<Double> predict(final INumericFeatureSupervisedInstance<Double>[] dTest) throws PredictionException, InterruptedException {
		return new PredictionBatch<>(Arrays.stream(dTest).map(x -> {
			try {
				return this.predict(x);
			} catch (PredictionException e) {
				LOGGER.error("There was an issue while making a prediction", e);
				return new Prediction<>(Double.NaN);
			} catch (InterruptedException e) {
				LOGGER.error("Got interrupted while predicting. ", e);
				Thread.currentThread().interrupt();
				return new Prediction<>(Double.NaN);
			}
		}).collect(Collectors.toList()));
	}

	@Override
	public void setConfig(final IWekaClassifierConfig config) throws LearnerConfigurationFailedException, InterruptedException {
		this.options = config.getOptions();
		try {
			this.wrappedClassifier.setOptions(this.options);
		} catch (Exception e) {
			throw new LearnerConfigurationFailedException("Could not set config for " + WekaClassifier.class.getSimpleName());
		}
	}

}
