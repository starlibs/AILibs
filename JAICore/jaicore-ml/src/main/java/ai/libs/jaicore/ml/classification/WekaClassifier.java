package ai.libs.jaicore.ml.classification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.algorithm.ILearningAlgorithm;
import org.api4.java.ai.ml.algorithm.LearningAlgorithmConfigurationFailedException;
import org.api4.java.ai.ml.algorithm.PredictionException;
import org.api4.java.ai.ml.algorithm.TrainingException;

import ai.libs.jaicore.ml.classification.multiclass.InconsistentDataFormatException;
import weka.classifiers.AbstractClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class WekaClassifier implements ILearningAlgorithm<double[], Double, String[]> {

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

	@Override
	public void setConfig(final String[] config) throws LearningAlgorithmConfigurationFailedException {
		this.options = config;
		try {
			this.wrappedClassifier.setOptions(config);
		} catch (Exception e) {
			throw new LearningAlgorithmConfigurationFailedException("Could not set config for " + WekaClassifier.class.getSimpleName());
		}
	}

	private Instances extractMetaData(final double[][] xBatch, final Double[] yBatch) {
		ArrayList<Attribute> attInfo = new ArrayList<>();
		for (int i = 0; i < xBatch[0].length; i++) {
			attInfo.add(new Attribute("a" + i));
		}
		this.targetValueToClass.clear();
		Arrays.stream(yBatch).forEach(yI -> this.targetValueToClass.computeIfAbsent(yI, t -> "c" + t));
		attInfo.add(new Attribute("class", new LinkedList<>(this.targetValueToClass.values())));

		Instances extractedMetaData = new Instances("Wrapped WEKA Instances", attInfo, 0);
		extractedMetaData.setClassIndex(extractedMetaData.numAttributes() - 1);
		return extractedMetaData;
	}

	@Override
	public void fit(final double[][] xBatchTrain, final Double[] yBatchTrain) throws TrainingException {
		if (xBatchTrain.length != yBatchTrain.length) { // check whether data has consistent format
			throw new InconsistentDataFormatException("The number of instances and labels is inconsistent as there are " + xBatchTrain.length + " instances and " + yBatchTrain.length + " labels");
		}

		// extract the dataset's meta data and prepare the instances.
		this.metaData = this.extractMetaData(xBatchTrain, yBatchTrain);

		Instances trainData = new Instances(this.metaData, 0);
		for (int i = 0; i < xBatchTrain.length; i++) {
			Instance newI = new DenseInstance(trainData.numAttributes());
			newI.setDataset(trainData);
			for (int j = 0; j < xBatchTrain[0].length; j++) {
				newI.setValue(j, xBatchTrain[i][j]);
			}
			newI.setValue(xBatchTrain[0].length, this.targetValueToClass.get(yBatchTrain[i]));
			trainData.add(newI);
		}
		try {
			this.wrappedClassifier.buildClassifier(trainData);
		} catch (Exception e) {
			throw new TrainingException("Could not build " + this.getClass().getSimpleName() + " due to exception", e);
		}
	}

	@Override
	public Double predict(final double[] xTest) throws PredictionException {
		Instance testInstance = new DenseInstance(this.metaData.numAttributes());
		testInstance.setDataset(this.metaData);
		IntStream.range(0, xTest.length).forEach(ix -> testInstance.setValue(ix, xTest[ix]));
		try {
			return this.wrappedClassifier.classifyInstance(testInstance);
		} catch (Exception e) {
			throw new PredictionException("Could not make a prediction since an exception occurred in the wrapped weka classifier.", e);
		}
	}

	@Override
	public Double[] predict(final double[][] xBatchTest) throws PredictionException {
		Double[] resultVector = new Double[xBatchTest.length];
		for (int i = 0; i < xBatchTest.length; i++) {
			resultVector[i] = this.predict(xBatchTest[i]);
		}
		return resultVector;
	}

	@Override
	public Double fitAndPredict(final double[][] xBatchTrain, final Double[] yBatchTrain, final double[] xTest) throws TrainingException, PredictionException {
		this.fit(xBatchTrain, yBatchTrain);
		return this.predict(xTest);
	}

	@Override
	public Double[] fitAndPredict(final double[][] xBatchTrain, final Double[] yBatchTrain, final double[][] xBatchTest) throws TrainingException, PredictionException {
		this.fit(xBatchTrain, yBatchTrain);
		return this.predict(xBatchTest);
	}

}
