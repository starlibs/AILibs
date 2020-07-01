package ai.libs.jaicore.ml.learningcurve.extrapolation.inversepowerlaw;

import java.io.File;
import java.io.FileReader;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

import ai.libs.jaicore.ml.scikitwrapper.EBasicProblemType;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
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
	public static ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> trainModelClassifier(final File datasetFolder, final String modelPath) throws Exception {
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
		WekaInstances newData = new WekaInstances(Filter.useFilter(data, removeFilter));
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor(activation='logistic', solver='lbfgs', max_iter=1000)", "from sklearn.neural_network import MLPRegressor", EBasicProblemType.REGRESSION);
		slw.setModelPath(new File(modelPath));
		slw.setProblemType(EBasicProblemType.REGRESSION);

		int s = newData.getNumAttributes();
		slw.setTargets(s - 3, s - 2, s - 1);
		slw.fit(newData);
		return slw;
	}

}
