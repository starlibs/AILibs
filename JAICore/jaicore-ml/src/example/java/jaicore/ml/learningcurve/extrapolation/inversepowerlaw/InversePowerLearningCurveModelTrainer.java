package jaicore.ml.learningcurve.extrapolation.inversepowerlaw;

import java.io.File;
import java.io.FileReader;

import jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import jaicore.ml.scikitwrapper.ScikitLearnWrapper.ProblemType;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * 
 * Utility class for training a Sk-Learn model for predicting Inverse Power Law
 * learning curves.
 * 
 * @author Lukas Brandt
 */
public class InversePowerLearningCurveModelTrainer {

	private InversePowerLearningCurveModelTrainer() {
	}

	/**
	 * Trains a predictor for Inverse Power law parameters with all the datasets in
	 * a folder. Afterwards the predictor will be serialized and saved.
	 * 
	 * @param datasetFolder
	 *            Folder with the datasets
	 * @param modelPath
	 *            Destination where the built model shall be saved.
	 * @return Trained classifier.
	 */
	public static Classifier trainModelClassifier(File datasetFolder, String modelPath) throws Exception {
		Instances data = null;
		for (File file : datasetFolder.listFiles()) {
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
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor(activation='logistic', solver='lbfgs', max_iter=1000)", "from sklearn.neural_network import MLPRegressor");
		slw.setModelPath(new File(modelPath));
		slw.setProblemType(ProblemType.REGRESSION);

		int s = newData.numAttributes();
		slw.setTargets(s - 3, s - 2, s - 1);
		slw.buildClassifier(newData);
		return slw;
	}

}
