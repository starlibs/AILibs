package jaicore.ml.learningcurve.extrapolation.InversePowerLaw;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;

import jaicore.ml.interfaces.LearningCurve;
import jaicore.ml.learningcurve.extrapolation.InvalidAnchorPointsException;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolationMethod;
import jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class InversePowerLawExtrapolator implements LearningCurveExtrapolationMethod {

	@Override
	public LearningCurve extrapolateLearningCurveFromAnchorPoints(int[] xValues, double[] yValues)
			throws InvalidAnchorPointsException {
		// Check if the anchorpoints for the neural net exist.
		double y8 = -1, y16 = -1, y64 = -1, y128 = -1;
		for (int i = 0; i < xValues.length; i++) {
			switch (xValues[i]) {
			case 8:
				y8 = yValues[i];
				break;
			case 16:
				y16 = yValues[i];
				break;
			case 64:
				y64 = yValues[i];
				break;
			case 128:
				y128 = yValues[i];
				break;
			}
		}
		if (y8 == -1 || y16 == -1 || y64 == -1 || y128 == -1) {
			throw new InvalidAnchorPointsException("Anchorpoints must be for x={8, 16, 64, 128}");
		}
		try {
			// Predict Inverse Power Law parameters from anchorpoints.
			ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor()",
					"from sklearn.neural_network import MLPRegressor");
			slw.setModelPath(Paths.get("resources/LearningCurveExtrapolation/InversePowerLawModel.pcl").toAbsolutePath()
					.toString());
			slw.setIsRegression(true);
			slw.setTargets(4, 5, 6);
			Instances data = new Instances(new FileReader("resources/LearningCurveExtrapolation/EmptyDataset.arff"));
			Instance newInstance = new DenseInstance(1, new double[] { y8, y16, y64, y128, 0, 0, 0 });
			Instance newInstance2 = new DenseInstance(1, new double[] { 0, 0, 0, 0, 0, 0, 0 });
			data.add(newInstance);
			data.add(newInstance2);
			data.setClassIndex(data.numAttributes() - 1);
			double[] result = slw.classifyInstances(data);
			double a = Math.max(0.0000000001, Math.min(result[3], 0.9999999999));
			double b = result[4];
			double c = Math.max(-0.9999999999, Math.min(result[5], -0.0000000001));
			return new InversePowerLawLearningCurve(a, b, c);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Trains a predictor for Inverse Power law parameters with all the datasets in
	 * a folder.
	 * 
	 * @param datasetFolder Folder with the datasets
	 * @param modelPath     Destination where the built model shall be saved.
	 * @return Trained classifier.
	 */
	private Classifier trainModelClassifier(File datasetFolder, String modelPath) throws Exception {
		Instances data = null;
		for (File file : datasetFolder.listFiles()) {
			System.out.println(file.getPath());
			if (data == null) {
				data = new Instances(new FileReader(file));
			} else {
				data.addAll(new Instances(new FileReader(file)));
			}
		}
		Remove removeFilter = new Remove();
		removeFilter.setAttributeIndicesArray(new int[] { 1, 2, 3, 4, 5, 6, 7 });
		removeFilter.setInvertSelection(true);
		removeFilter.setInputFormat(data);
		Instances newData = Filter.useFilter(data, removeFilter);
		ScikitLearnWrapper slw = new ScikitLearnWrapper(
				"MLPRegressor(activation='logistic', solver='lbfgs', max_iter=1000)",
				"from sklearn.neural_network import MLPRegressor");
		slw.setIsRegression(true);
		slw.setModelPath(modelPath);
		int s = newData.numAttributes();
		slw.setTargets(s - 3, s - 2, s - 1);
		slw.buildClassifier(newData);

		return slw;
	}

}
