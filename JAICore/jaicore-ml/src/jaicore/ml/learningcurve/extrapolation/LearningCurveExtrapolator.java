package jaicore.ml.learningcurve.extrapolation;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.SubsamplingMethod;
import jaicore.ml.core.dataset.weka.WekaInstancesUtil;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Abstract class for implementing a learning curve extrapolation method with
 * some anchor points. For each of this anchorpoints a subsample will be drawn
 * and a classifier will be trained with this sample. Based on the points
 * (subsample size, learner accuracy) a custom method of learning curve
 * extrapolation can be applied.
 * 
 * @author Lukas Brandt
 */
public abstract class LearningCurveExtrapolator {

	protected Classifier learner;
	protected IDataset<IInstance> dataset;
	protected ASamplingAlgorithm<IInstance> subsamplingAlgorithm;

	/**
	 * Create a learning curve extrapolator with a custom configured subsampling
	 * method.
	 * 
	 * @param learner           Learning model to predict the learning curve of.
	 * @param dataset           Dataset to measure evaluate the learner on.
	 * @param subsamplingMethod Subsampling method to retrieve a default configured
	 *                          subsampler for.
	 * @param seed              Random seed for the subsampler.
	 */
	public LearningCurveExtrapolator(Classifier learner, IDataset<IInstance> dataset,
			SubsamplingMethod subsamplingMethod, long seed) {
		this.learner = learner;
		this.dataset = dataset;
		this.subsamplingAlgorithm = subsamplingMethod.getSubsampler(seed);
	}

	/**
	 * Create a learning curve extrapolator with a given subsampler.
	 * 
	 * @param learner              Learning model to predict the learning curve of.
	 * @param dataset              Dataset to measure evaluate the learner on.
	 * @param subsamplingAlgorithm Subsampler to create the samples at the
	 *                             anchorpoints.
	 */
	public LearningCurveExtrapolator(Classifier learner, IDataset<IInstance> dataset,
			ASamplingAlgorithm<IInstance> subsamplingAlgorithm) {
		this.learner = learner;
		this.dataset = dataset;
		this.subsamplingAlgorithm = subsamplingAlgorithm;
	}

	/**
	 * Measure the learner accuracy at the given anchorpoints and extrapolate a
	 * learning curve based the results.
	 * 
	 * @param anchorPoints Sample sizes as anchorpoints, where the accuracy shall be
	 *                     measured.
	 * @return The extrapolated learning curve.
	 */
	public ExtrapolatedLearningcurve extapolateLearningCurve(int[] anchorPoints) throws Exception {
		double[] yValues = new double[anchorPoints.length];

		// Create test and train splits.
		IDataset<IInstance> train = this.dataset.createEmpty();
		IDataset<IInstance> test = this.dataset.createEmpty();
		int testSplitStartIndex = (int) (this.dataset.size() * 0.8d);
		for (int i = 0; i < dataset.size(); i++) {
			if (i < testSplitStartIndex) {
				train.add(this.dataset.get(i));
			} else {
				test.add(this.dataset.get(i));
			}
		}
		Instances testInstances = WekaInstancesUtil.datasetToWekaInstances(test);

		// Create subsamples at the anchorpoints and measure the accuracy there.
		for (int i = 0; i < anchorPoints.length; i++) {

			this.subsamplingAlgorithm.setSampleSize(anchorPoints[i]);
			this.subsamplingAlgorithm.setInput(train);
			IDataset<IInstance> subsampledDataset = this.subsamplingAlgorithm.call();

			// Train classifier on subsampler.
			this.learner.buildClassifier(WekaInstancesUtil.datasetToWekaInstances(subsampledDataset));

			// Measure accuracy of learner on test split.
			double correctCounter = 0d;
			for (Instance instance : testInstances) {
				if (this.learner.classifyInstance(instance) == instance.classValue()) {
					correctCounter++;
				}
			}
			yValues[i] = correctCounter / (double) test.size();
		}

		return extrapolateLearningCurveFromAnchorPoints(anchorPoints, yValues);
	}

	protected abstract ExtrapolatedLearningcurve extrapolateLearningCurveFromAnchorPoints(int[] xValues,
			double[] yValues);

}
