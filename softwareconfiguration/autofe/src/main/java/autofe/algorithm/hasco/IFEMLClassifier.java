package autofe.algorithm.hasco;

import org.nd4j.linalg.api.ndarray.INDArray;

import ai.libs.jaicore.ml.core.exception.PredictionException;
import ai.libs.jaicore.ml.core.exception.TrainingException;
import autofe.util.DataSet;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public interface IFEMLClassifier extends Classifier {

	public void buildClassifier(final DataSet data) throws TrainingException;

	public double classifyInstance(final INDArray instance, final Instances refInstances) throws PredictionException;

	public double[] distributionForInstance(final INDArray instance, final Instances refInstances) throws PredictionException;

	public Instances transformData(final DataSet data) throws InterruptedException;

	public Instance transformData(final INDArray instance, final Instances refInstances) throws InterruptedException;

}
