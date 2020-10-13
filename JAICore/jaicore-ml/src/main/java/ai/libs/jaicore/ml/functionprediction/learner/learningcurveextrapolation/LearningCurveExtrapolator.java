package ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.evaluation.learningcurve.ILearningCurve;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;

/**
 * Abstract class for implementing a learning curve extrapolation method with
 * some anchor points. For each of this anchorpoints a subsample will be drawn
 * and a classifier will be trained with this sample. Based on the points
 * (subsample size, learner accuracy) a custom method of learning curve
 * extrapolation can be applied.
 *
 * @author Lukas Brandt
 * @author Felix Mohr
 */
public class LearningCurveExtrapolator implements ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(LearningCurveExtrapolator.class);

	protected ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner;
	protected ILabeledDataset<? extends ILabeledInstance> dataset;
	protected ILabeledDataset<? extends ILabeledInstance> train;
	protected ILabeledDataset<? extends ILabeledInstance> test;
	protected ISamplingAlgorithmFactory<ILabeledDataset<?>, ? extends ASamplingAlgorithm<ILabeledDataset<?>>> samplingAlgorithmFactory;
	protected ASamplingAlgorithm<ILabeledDataset<? extends ILabeledInstance>> samplingAlgorithm;
	protected Random random;
	protected LearningCurveExtrapolationMethod extrapolationMethod;
	private final int[] anchorPoints;
	private final double[] yValues;
	private final int[] trainingTimes;

	/**
	 * Create a learning curve extrapolator with a subsampling factory.
	 *
	 * @param extrapolationMethod Method for extrapolating a learning curve
	 *            from anchorpoints.
	 * @param learner Learning model to predict the learning curve
	 *            of.
	 * @param dataset Dataset to measure evaluate the learner on.
	 * @param trainsplit Portion of the dataset, which shall be used
	 *            to sample from for training.
	 * @param samplingAlgorithmFactory Subsampling algorithm factory to create a
	 *            configured subsampler with.
	 * @param seed Random seed.
	 * @throws DatasetCreationException
	 * @throws InterruptedException
	 */
	public LearningCurveExtrapolator(final LearningCurveExtrapolationMethod extrapolationMethod, final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner, final ILabeledDataset<?> dataset,
			final double trainsplit, final int[] anchorPoints, final ISamplingAlgorithmFactory<ILabeledDataset<?>, ? extends ASamplingAlgorithm<ILabeledDataset<?>>> samplingAlgorithmFactory, final long seed)
			throws DatasetCreationException, InterruptedException {
		this.extrapolationMethod = extrapolationMethod;
		this.learner = learner;
		this.dataset = dataset;
		this.anchorPoints = anchorPoints;
		this.samplingAlgorithmFactory = samplingAlgorithmFactory;
		this.samplingAlgorithm = null;
		this.random = new Random(seed);
		this.createSplit(trainsplit, seed);
		this.yValues = new double[this.anchorPoints.length];
		this.trainingTimes = new int[this.anchorPoints.length];
	}

	/**
	 * Measure the learner accuracy at the given anchorpoints and extrapolate a
	 * learning curve based the results.
	 *
	 * @param anchorPoints Sample sizes as anchorpoints, where the true accuracy
	 *            shall be measured.
	 * @return The extrapolated learning curve.
	 *
	 * @throws InvalidAnchorPointsException The anchorpoints (amount, values, ...)
	 *             are not suitable for the given learning
	 *             curve extrapolation method.
	 * @throws AlgorithmException An error occured during the creation of
	 *             the specified anchorpoints.
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	public ILearningCurve extrapolateLearningCurve() throws InvalidAnchorPointsException, AlgorithmException, InterruptedException {
		try {
			ILabeledDataset<? extends ILabeledInstance> testInstances = this.test;

			// Create subsamples at the anchorpoints and measure the accuracy there.
			SupervisedLearnerExecutor learnerExecutor = new SupervisedLearnerExecutor();
			IDeterministicPredictionPerformanceMeasure<Integer, ISingleLabelClassification> metric = EClassificationPerformanceMeasure.ERRORRATE;
			for (int i = 0; i < this.anchorPoints.length; i++) {

				// If it is a rerunnable factory, set the previous run.
				if (this.samplingAlgorithmFactory instanceof IRerunnableSamplingAlgorithmFactory && this.samplingAlgorithm != null) {
					((IRerunnableSamplingAlgorithmFactory<ILabeledDataset<?>, ASamplingAlgorithm<ILabeledDataset<?>>>) this.samplingAlgorithmFactory).setPreviousRun(this.samplingAlgorithm);
				}
				this.samplingAlgorithm = this.samplingAlgorithmFactory.getAlgorithm(this.anchorPoints[i], this.train, this.random);
				ILabeledDataset<? extends ILabeledInstance> subsampledDataset = this.samplingAlgorithm.call();

				// Train classifier on subsample.
				this.logger.debug("Running classifier with {} data points.", this.anchorPoints[i]);
				ILearnerRunReport report = learnerExecutor.execute(this.learner, subsampledDataset, testInstances);
				this.trainingTimes[i] = (int) (report.getTrainEndTime() - report.getTrainStartTime());

				// Measure accuracy of the trained learner on test split.
				this.yValues[i] = metric.loss(report.getPredictionDiffList().getCastedView(Integer.class, ISingleLabelClassification.class));
				this.logger.debug("Training finished. Observed learning curve value (accuracy) of {}.", this.yValues[i]);
			}
			if (this.logger.isInfoEnabled()) {
				this.logger.info("Computed accuracies of {} for anchor points {}. Now extrapolating a curve from these observations.", Arrays.toString(this.yValues), Arrays.toString(this.anchorPoints));
			}
			return this.extrapolationMethod.extrapolateLearningCurveFromAnchorPoints(this.anchorPoints, this.yValues, this.dataset.size());
		} catch (AlgorithmExecutionCanceledException | TimeoutException | AlgorithmException e) {
			throw new AlgorithmException("Error during creation of the subsamples for the anchorpoints", e);
		} catch (ExecutionException e) {
			throw new AlgorithmException("Error during learning curve extrapolation", e);
		} catch (InvalidAnchorPointsException | InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new AlgorithmException("Error during training/testing the classifier", e);
		}

	}

	private void createSplit(final double trainsplit, final long seed) throws DatasetCreationException, InterruptedException {
		long start = System.currentTimeMillis();
		this.logger.debug("Creating split with training portion {} and seed {}", trainsplit, seed);
		Random r = new Random(seed);
		try {
			List<ILabeledDataset<?>> folds = SplitterUtil.getLabelStratifiedTrainTestSplit(this.dataset, seed, trainsplit);
			this.train = folds.get(0);
			this.test = folds.get(1);

			// Shuffle train and test
			this.logger.debug("Shuffling train and test data");
			Collections.shuffle(this.train, r);
			Collections.shuffle(this.test, r);
			this.logger.debug("Finished split creation after {}ms", System.currentTimeMillis() - start);
		} catch (SplitFailedException e) {
			throw new DatasetCreationException(e);
		}

	}

	public ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> getLearner() {
		return this.learner;
	}

	public ILabeledDataset<?> getDataset() {
		return this.dataset;
	}

	public LearningCurveExtrapolationMethod getExtrapolationMethod() {
		return this.extrapolationMethod;
	}

	public int[] getAnchorPoints() {
		return this.anchorPoints;
	}

	public double[] getyValues() {
		return this.yValues;
	}

	public int[] getTrainingTimes() {
		return this.trainingTimes;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

}
