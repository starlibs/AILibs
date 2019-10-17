package ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.learningcurve.ILearningCurve;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 */
public class LearningCurveExtrapolator<I extends ILabeledInstance, D extends ILabeledDataset<I>>
		implements ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(LearningCurveExtrapolator.class);

	protected IClassifier<I, D> learner;
	protected D dataset;
	protected D train;
	protected D test;
	protected ISamplingAlgorithmFactory<I, D, ? extends ASamplingAlgorithm<I, D>> samplingAlgorithmFactory;
	protected ASamplingAlgorithm<I, D> samplingAlgorithm;
	protected Random random;
	protected LearningCurveExtrapolationMethod extrapolationMethod;
	private final int[] anchorPoints;
	private final double[] yValues;
	private final int[] trainingTimes;

	/**
	 * Create a learning curve extrapolator with a subsampling factory.
	 *
	 * @param extrapolationMethod      Method for extrapolating a learning curve
	 *                                 from anchorpoints.
	 * @param learner                  Learning model to predict the learning curve
	 *                                 of.
	 * @param dataset                  Dataset to measure evaluate the learner on.
	 * @param trainsplit               Portion of the dataset, which shall be used
	 *                                 to sample from for training.
	 * @param samplingAlgorithmFactory Subsampling algorithm factory to create a
	 *                                 configured subsampler with.
	 * @param seed                     Random seed.
	 * @throws DatasetCreationException
	 * @throws InterruptedException
	 */
	public LearningCurveExtrapolator(final LearningCurveExtrapolationMethod extrapolationMethod,
			final IClassifier<I, D> learner, final D dataset, final double trainsplit, final int[] anchorPoints,
			final ISamplingAlgorithmFactory<I, D, ? extends ASamplingAlgorithm<I, D>> samplingAlgorithmFactory,
			final long seed) throws DatasetCreationException, InterruptedException {
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
	 *                     shall be measured.
	 * @return The extrapolated learning curve.
	 *
	 * @throws InvalidAnchorPointsException The anchorpoints (amount, values, ...)
	 *                                      are not suitable for the given learning
	 *                                      curve extrapolation method.
	 * @throws AlgorithmException           An error occured during the creation of
	 *                                      the specified anchorpoints.
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	public ILearningCurve extrapolateLearningCurve()
			throws InvalidAnchorPointsException, AlgorithmException, InterruptedException {
		try {
			D testInstances = this.test;

			// Create subsamples at the anchorpoints and measure the accuracy there.
			for (int i = 0; i < this.anchorPoints.length; i++) {

				// If it is a rerunnable factory, set the previous run.
				if (this.samplingAlgorithmFactory instanceof IRerunnableSamplingAlgorithmFactory
						&& this.samplingAlgorithm != null) {
					((IRerunnableSamplingAlgorithmFactory<I, D, ASamplingAlgorithm<I, D>>) this.samplingAlgorithmFactory)
							.setPreviousRun(this.samplingAlgorithm);
				}
				this.samplingAlgorithm = this.samplingAlgorithmFactory.getAlgorithm(this.anchorPoints[i], this.train,
						this.random);
				D subsampledDataset = this.samplingAlgorithm.call();

				// Train classifier on subsample.
				this.logger.debug("Running classifier with {} data points.", this.anchorPoints[i]);
				long start = System.currentTimeMillis();
				this.learner.fit(subsampledDataset);
				this.trainingTimes[i] = (int) (System.currentTimeMillis() - start);

				// Measure accuracy of the trained learner on test split.
				double correctCounter = 0d;
				for (I instance : testInstances) {
					if (this.learner.predict(instance).equals(instance.getLabel())) {
						correctCounter++;
					}
				}
				this.yValues[i] = correctCounter / testInstances.size();
				this.logger.debug("Training finished. Observed learning curve value (accuracy) of {}.",
						this.yValues[i]);
			}
			if (this.logger.isInfoEnabled()) {
				this.logger.info(
						"Computed accuracies of {} for anchor points {}. Now extrapolating a curve from these observations.",
						Arrays.toString(this.yValues), Arrays.toString(this.anchorPoints));
			}
			return this.extrapolationMethod.extrapolateLearningCurveFromAnchorPoints(this.anchorPoints, this.yValues,
					this.dataset.size());
		}  catch (AlgorithmExecutionCanceledException | TimeoutException | AlgorithmException e) {
			throw new AlgorithmException("Error during creation of the subsamples for the anchorpoints", e);
		} catch (ExecutionException e) {
			throw new AlgorithmException("Error during learning curve extrapolation", e);
		} catch (InvalidAnchorPointsException | InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new AlgorithmException("Error during training/testing the classifier", e);
		}

	}

	@SuppressWarnings("unchecked")
	private void createSplit(final double trainsplit, final long seed)
			throws DatasetCreationException, InterruptedException {
		long start = System.currentTimeMillis();
		this.logger.debug("Creating split with training portion {} and seed {}", trainsplit, seed);
		Random r = new Random(seed);
		this.train = (D) this.dataset.createEmptyCopy();
		this.test = (D) this.dataset.createEmptyCopy();
		D data = (D) this.dataset.createEmptyCopy();
		data.addAll(this.dataset);

		// Shuffle the data
		Collections.shuffle(data, r);

		// Stratify the data by class
		Map<Object, D> classStrati = new HashMap<>();
		for (I d : this.dataset) {
			Object c = d.getLabel();
			if (!classStrati.containsKey(c)) {
				classStrati.put(c, (D) this.dataset.createEmptyCopy());
			}
			classStrati.get(c).add(d);
		}

		// Retrieve strati sizes
		Map<Object, Integer> classStratiSizes = new HashMap<>(classStrati.size());
		for (Entry<Object, D> entry : classStrati.entrySet()) {
			classStratiSizes.put(entry.getKey(), classStrati.get(entry.getKey()).size());
		}

		// First assign one item of each class to train and test
		for (Entry<Object, D> entry : classStrati.entrySet()) {
			D availableInstances = classStrati.get(entry.getKey());
			if (!availableInstances.isEmpty()) {
				this.train.add(availableInstances.get(0));
				availableInstances.remove(0);
			}
			if (!availableInstances.isEmpty()) {
				this.test.add(availableInstances.get(0));
				availableInstances.remove(0);
			}
		}

		// Distribute remaining instances over train test
		for (Entry<Object, D> entry : classStrati.entrySet()) {
			D availableInstances = classStrati.get(entry.getKey());
			int trainItems = (int) Math.min(availableInstances.size(),
					Math.ceil(trainsplit * classStratiSizes.get(entry.getKey())));
			for (int j = 0; j < trainItems; j++) {
				this.train.add(availableInstances.get(0));
				availableInstances.remove(0);
			}
			int testItems = (int) Math.min(availableInstances.size(),
					Math.ceil((1 - trainsplit) * classStratiSizes.get(entry.getKey())));
			for (int j = 0; j < testItems; j++) {
				this.test.add(availableInstances.get(0));
				availableInstances.remove(0);
			}
		}

		// Shuffle train and test
		this.logger.debug("Shuffling train and test data");
		Collections.shuffle(this.train, r);
		Collections.shuffle(this.test, r);
		this.logger.debug("Finished split creation after {}ms", System.currentTimeMillis() - start);

	}

	public IClassifier<I, D> getLearner() {
		return this.learner;
	}

	public D getDataset() {
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
