package ai.libs.jaicore.ml.weka.learner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.exception.LearnerConfigurationFailedException;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.core.evaluation.Prediction;
import ai.libs.jaicore.ml.core.evaluation.PredictionBatch;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class WekaClassifier extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<ILabeledInstance>> implements IWekaClassifier {
	private static final Logger LOGGER = LoggerFactory.getLogger(WekaClassifier.class);

	private final String name;
	private String[] options;
	private Classifier wrappedClassifier;
	private Instances metaData;
	private Map<Double, String> targetValueToClass = new HashMap<>();

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
	}

	public String getName() {
		return this.name;
	}

	public String[] getOptions() {
		return this.options;
	}

	private Instances extractMetaData(final ILabeledDataset<ILabeledInstance> dTrain) {
		System.out.println("Feature types: " + dTrain.getListOfAttributes());
		ArrayList<Attribute> attInfo = new ArrayList<>();
		for (int i = 0; i < dTrain.getListOfAttributes().size(); i++) {
			IAttribute type = dTrain.getListOfAttributes().get(i);
			if (type instanceof ICategoricalAttribute) {
				attInfo.add(new Attribute("a" + i, ((ICategoricalAttribute) type).getValues()));
			} else if (type instanceof INumericAttribute) {
				attInfo.add(new Attribute("a" + i));
			}
		}

		IAttribute labelType = dTrain.getLabelAttribute();
		if (labelType instanceof ICategoricalAttribute) {
			ICategoricalAttribute nomLabel = (ICategoricalAttribute) labelType;
			attInfo.add(new Attribute("class", new LinkedList<>(nomLabel.getValues())));
			this.targetValueToClass.clear();
			for (String value : ((ICategoricalAttribute) labelType).getValues()) {
				this.targetValueToClass.put(nomLabel.toDouble(value), value);
			}
		} else {
			attInfo.add(new Attribute("class"));
		}

		Instances extractedMetaData = new Instances("Wrapped WEKA Instances", attInfo, 0);
		extractedMetaData.setClassIndex(extractedMetaData.numAttributes() - 1);
		return extractedMetaData;
	}

	@Override
	public void fit(final ILabeledDataset<ILabeledInstance> dTrain) throws TrainingException, InterruptedException {
		WekaInstances data = new WekaInstances(dTrain);
		// extract the dataset's meta data and prepare the instances.
		this.metaData = this.extractMetaData(dTrain);
		System.out.println(this.metaData);

		Instances trainData = new Instances(this.metaData, 0);
		for (int i = 0; i < dTrain.size(); i++) {
			Instance newI = new DenseInstance(trainData.numAttributes());
			newI.setDataset(trainData);
			for (int j = 0; j < dTrain.get(i).getNumAttributes(); j++) {
				if (dTrain.getListOfAttributes().get(j) instanceof ICategoricalAttribute) {
					newI.setValue(j, ((ICategoricalAttribute) dTrain.getListOfAttributes().get(j)).getAsAttributeValue(dTrain.get(i).getAttributeValue(j)).getValue());
				} else if (dTrain.getListOfAttributes().get(j) instanceof INumericAttribute) {
					newI.setValue(j, dTrain.getAttribute(j).toDouble(dTrain.get(i).getAttributeValue(j)));
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
	public IPrediction predict(final ILabeledInstance xTest) throws PredictionException, InterruptedException {
		Instance testInstance = new DenseInstance(this.metaData.numAttributes());
		testInstance.setDataset(this.metaData);
		IntStream.range(0, xTest.getNumAttributes()).forEach(ix -> testInstance.setValue(ix, xTest.getAttribute(ix).toDouble(xTest.getAttribute(ix))));
		try {
			return new Prediction(this.wrappedClassifier.classifyInstance(testInstance));
		} catch (Exception e) {
			throw new PredictionException("Could not make a prediction since an exception occurred in the wrapped weka classifier.", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public IPredictionBatch predict(final ILabeledDataset<ILabeledInstance> dTest) throws PredictionException, InterruptedException {
		return this.predict((ILabeledInstance[]) dTest.stream().toArray());
	}

	@Override
	public IPredictionBatch predict(final ILabeledInstance[] dTest) throws PredictionException, InterruptedException {
		return new PredictionBatch(Arrays.stream(dTest).map(x -> {
			try {
				return this.predict(x);
			} catch (PredictionException e) {
				LOGGER.error("There was an issue while making a prediction", e);
				return new Prediction(Double.NaN);
			} catch (InterruptedException e) {
				LOGGER.error("Got interrupted while predicting. ", e);
				Thread.currentThread().interrupt();
				return new Prediction(Double.NaN);
			}
		}).collect(Collectors.toList()));
	}

	@Override
	public void setConfig(final Map<String, Object> config) throws LearnerConfigurationFailedException, InterruptedException {
		try {
			this.wrappedClassifier.setOptions(this.options);
		} catch (Exception e) {
			throw new LearnerConfigurationFailedException("Could not set config for " + WekaClassifier.class.getSimpleName());
		}
	}

	@Override
	public Classifier getClassifier() {
		return this.wrappedClassifier;
	}

}
