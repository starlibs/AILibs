package jaicore.ml.evaluation.evaluators.weka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import jaicore.ml.interfaces.LearningCurve;
import jaicore.ml.learningcurve.extrapolation.InvalidAnchorPointsException;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolationMethod;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolator;
import weka.classifiers.Classifier;

/**
 * Evaluates a classifier by predicting its learning curve with a few
 * anchorpoints. The evaluation result is the predicted accuracy for the
 * complete dataset. Depending on the chosen anchorpoints this evaluation method
 * will be really fast, but can be inaccurate depending on the learning curve
 * extrapolation method, since it will only give a prediction of the accuracy
 * and does not measure it.
 * 
 * @author Lukas Brandt
 */
public class LearningCurveExtrapolationEvaluator implements IClassifierEvaluator {

	private final static Logger logger = LoggerFactory.getLogger(LearningCurveExtrapolationEvaluator.class);

	// Configuration for the learning curve extrapolator.
	private int[] anchorpoints;
	private ISamplingAlgorithmFactory<IInstance, ? extends ASamplingAlgorithm<IInstance>> samplingAlgorithmFactory;
	private IDataset<IInstance> dataset;
	private double trainSplitForAnchorpointsMeasurement;
	private LearningCurveExtrapolationMethod extrapolationMethod;
	private long seed;
	private int fullDatasetSize = -1;

	/**
	 * Create a classifier evaluator with learning curve extrapolation.
	 * 
	 * @param anchorpoints                         Anchorpoints for the learning
	 *                                             curve extrapolation.
	 * @param samplingAlgorithmFactory             Subsampling factory to create a
	 *                                             subsampler for the samples at the
	 *                                             given anchorpoints.
	 * @param dataset                              Dataset to evaluate the
	 *                                             classifier with.
	 * @param trainSplitForAnchorpointsMeasurement Ratio to split the subsamples at
	 *                                             the anchorpoints into train and
	 *                                             test.
	 * @param extrapolationMethod                  Method to extrapolate a learning
	 *                                             curve from the accuracy
	 *                                             measurements at the anchorpoints.
	 * @param seed                                 Random seed.
	 */
	public LearningCurveExtrapolationEvaluator(int[] anchorpoints,
			ISamplingAlgorithmFactory<IInstance, ? extends ASamplingAlgorithm<IInstance>> samplingAlgorithmFactory,
			IDataset<IInstance> dataset, double trainSplitForAnchorpointsMeasurement,
			LearningCurveExtrapolationMethod extrapolationMethod, long seed) {
		super();
		this.anchorpoints = anchorpoints;
		this.samplingAlgorithmFactory = samplingAlgorithmFactory;
		this.dataset = dataset;
		this.trainSplitForAnchorpointsMeasurement = trainSplitForAnchorpointsMeasurement;
		this.extrapolationMethod = extrapolationMethod;
		this.seed = seed;
	}

	public void setFullDatasetSize(int fullDatasetSize) {
		this.fullDatasetSize = fullDatasetSize;
	}

	@Override
	public Double evaluate(Classifier classifier)
			throws InterruptedException, ObjectEvaluationFailedException {
		// Create the learning curve extrapolator with the given configuration.
		LearningCurveExtrapolator extrapolator = new LearningCurveExtrapolator(this.extrapolationMethod, classifier,
				dataset, this.trainSplitForAnchorpointsMeasurement, this.samplingAlgorithmFactory, this.seed);

		try {
			// Create the extrapolator and calculate the accuracy the classifier would have
			// if it was trained on the complete dataset.
			LearningCurve learningCurve = extrapolator.extrapolateLearningCurve(this.anchorpoints);

			int evaluationPoint = dataset.size();
			// Overwrite evaluation point if a value was provided, otherwise evaluate on the
			// size of the given dataset
			if (this.fullDatasetSize != -1) {
				evaluationPoint = this.fullDatasetSize;
			}

			return learningCurve.getCurveValue(evaluationPoint) * 100.0d;
		} catch (AlgorithmException | InvalidAnchorPointsException e) {
			logger.warn("Evaluation of classifier failed due Exception {} with message {}. Returning null.",
					e.getClass().getName(), e.getMessage());
			return null;
		}
	}

}
