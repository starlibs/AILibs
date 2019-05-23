package autofe.algorithm.hasco;

import org.nd4j.linalg.api.ndarray.INDArray;

import autofe.util.DataSet;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public interface IFEMLClassifier extends Classifier {

	public void buildClassifier(final DataSet data) throws Exception;

	public double classifyInstance(final INDArray instance, final Instances refInstances) throws Exception;

	public double[] distributionForInstance(final INDArray instance, final Instances refInstances) throws Exception;

	public Instances transformData(final DataSet data) throws InterruptedException;

	public Instance transformData(final INDArray instance, final Instances refInstances) throws InterruptedException;

}
