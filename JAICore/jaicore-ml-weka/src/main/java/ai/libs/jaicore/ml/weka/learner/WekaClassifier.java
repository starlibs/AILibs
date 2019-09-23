package ai.libs.jaicore.ml.weka.learner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.exception.LearnerConfigurationFailedException;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.dataset.schema.attribute.INominalAttribute;
import org.api4.java.ai.ml.dataset.schema.attribute.INumericAttribute;
import org.api4.java.ai.ml.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.dataset.supervised.INumericFeatureSupervisedInstance;
import org.api4.java.ai.ml.learner.algorithm.IPrediction;
import org.api4.java.ai.ml.learner.algorithm.IPredictionBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.refactor.evaluation.Prediction;
import ai.libs.jaicore.ml.refactor.evaluation.PredictionBatch;
import ai.libs.jaicore.ml.refactor.learner.ASupervisedLearner;
import weka.classifiers.AbstractClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class WekaClassifier extends ASupervisedLearner<IWekaClassifierConfig, IWekaInstance, IWekaDataset<IWekaInstance>{
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

	private Instances extractMetaData(final IWekaDataset<IWekaInstance> dTrain) {
		System.out.println("Feature types: " + dTrain.getFeatureTypes());
		ArrayList<Attribute> attInfo = new ArrayList<>();
		for (int i = 0; i < dTrain.getFeatureTypes().size(); i++) {
			IAttribute type = dTrain.getFeatureTypes().getAttributeValue(i);
			if (type instanceof INominalAttribute) {
				attInfo.add(new Attribute("a" + i, ((INominalAttribute) type).getValues()));
			} else if (type instanceof INumericAttribute) {
				attInfo.add(new Attribute("a" + i));
			}
		}

		if (dTrain.getLabelTypes().isEmpty() || dTrain.getLabelTypes().size() > 1) {
			throw new UnsupportedOperationException("No target resp. multiple targets are not supported.");
		}

		IAttribute labelType = dTrain.getLabelTypes().get(0);
		if (labelType instanceof INominalAttribute) {
			INominalAttribute nomLabel = (INominalAttribute) labelType;
			attInfo.add(new Attribute("class", new LinkedList<>(nomLabel.getValues())));
			this.targetValueToClass.clear();
			for (String value : ((INominalAttribute) labelType).getValues()) {
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
	public void fit(final ILabeledDataset<Double, Double, INumericFeatureSupervisedInstance<Double>> dTrain) throws TrainingException, InterruptedException {
		// extract the dataset's meta data and prepare the instances.
		this.metaData = this.extractMetaData(dTrain);
		System.out.println(this.metaData);

		Instances trainData = new Instances(this.metaData, 0);
		for (int i = 0; i < dTrain.size(); i++) {
			Instance newI = new DenseInstance(trainData.numAttributes());
			newI.setDataset(trainData);
			for (int j = 0; j < dTrain.get(i).getNumAttributes(); j++) {
				if (dTrain.getFeatureTypes().getAttributeValue(j) instanceof INominalAttribute) {
					newI.setValue(j, ((INominalAttribute) dTrain.getFeatureTypes().getAttributeValue(j)).decodeToString(dTrain.get(i).getAttributeValue(j)));
				} else {
					newI.setValue(j, dTrain.get(i).getAttributeValue(j));
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
		IntStream.range(0, xTest.getNumAttributes()).forEach(ix -> testInstance.setValue(ix, xTest.getAttributeValue(ix)));
		try {
			return new Prediction<>(this.wrappedClassifier.classifyInstance(testInstance));
		} catch (Exception e) {
			throw new PredictionException("Could not make a prediction since an exception occurred in the wrapped weka classifier.", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public IPredictionBatch<Double> predict(final ILabeledDataset<Double, Double, INumericFeatureSupervisedInstance<Double>> dTest) throws PredictionException, InterruptedException {
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
