package jaicore.ml.learningcurve.extrapolation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import jaicore.ml.core.dataset.weka.WekaInstances;
import jaicore.ml.interfaces.LearningCurve;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.UnsupportedAttributeTypeException;

/**
 * Abstract class for implementing a learning curve extrapolation method with
 * some anchor points. For each of this anchorpoints a subsample will be drawn
 * and a classifier will be trained with this sample. Based on the points
 * (subsample size, learner accuracy) a custom method of learning curve
 * extrapolation can be applied.
 *
 * @author Lukas Brandt
 */
public class LearningCurveExtrapolator<I extends IInstance> implements ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(LearningCurveExtrapolator.class);

	protected Classifier learner;
	protected IDataset<I> dataset;
	protected IDataset<I> train;
	protected IDataset<I> test;
	protected ISamplingAlgorithmFactory<I, ? extends ASamplingAlgorithm<I>> samplingAlgorithmFactory;
	protected ASamplingAlgorithm<I> samplingAlgorithm;
	protected Random random;
	protected LearningCurveExtrapolationMethod extrapolationMethod;
	private final int[] anchorPoints;
	private final double[] yValues;
	private final int[] trainingTimes;

	/**
	 * Create a learning curve extrapolator with a subsampling factory.
	 *
	 * @param extrapolationMethod
	 *            Method for extrapolating a learning curve from anchorpoints.
	 * @param learner
	 *            Learning model to predict the learning curve of.
	 * @param dataset
	 *            Dataset to measure evaluate the learner on.
	 * @param trainsplit
	 *            Portion of the dataset, which shall be used to sample from for
	 *            training.
	 * @param samplingAlgorithmFactory
	 *            Subsampling algorithm factory to create a configured subsampler
	 *            with.
	 * @param seed
	 *            Random seed.
	 */
	public LearningCurveExtrapolator(final LearningCurveExtrapolationMethod extrapolationMethod, final Classifier learner, final IDataset<I> dataset, final double trainsplit, final int[] anchorPoints,
			final ISamplingAlgorithmFactory<I, ? extends ASamplingAlgorithm<I>> samplingAlgorithmFactory, final long seed) {
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
	 * @param anchorPoints
	 *            Sample sizes as anchorpoints, where the true accuracy shall be
	 *            measured.
	 * @return The extrapolated learning curve.
	 *
	 * @throws InvalidAnchorPointsException
	 *             The anchorpoints (amount, values, ...) are not suitable for the
	 *             given learning curve extrapolation method.
	 * @throws AlgorithmException
	 *             An error occured during the creation of the specified
	 *             anchorpoints.
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	public LearningCurve extrapolateLearningCurve() throws InvalidAnchorPointsException, AlgorithmException, InterruptedException {
		try {
			Instances testInstances = ((WekaInstances) this.test).getList();

			// Create subsamples at the anchorpoints and measure the accuracy there.
			for (int i = 0; i < this.anchorPoints.length; i++) {

				// If it is a rerunnable factory, set the previous run.
				if (this.samplingAlgorithmFactory instanceof IRerunnableSamplingAlgorithmFactory && this.samplingAlgorithm != null) {
					((IRerunnableSamplingAlgorithmFactory<I, ASamplingAlgorithm<I>>) this.samplingAlgorithmFactory).setPreviousRun(this.samplingAlgorithm);
				}
				this.samplingAlgorithm = this.samplingAlgorithmFactory.getAlgorithm(this.anchorPoints[i], this.train, this.random);
				IDataset<I> subsampledDataset = this.samplingAlgorithm.call();

				// Train classifier on subsample.
				this.logger.debug("Running classifier with {} data points.", this.anchorPoints[i]);
				long start = System.currentTimeMillis();
				this.learner.buildClassifier(((WekaInstances) subsampledDataset).getList());
				this.trainingTimes[i] = (int) (System.currentTimeMillis() - start);

				// Measure accuracy of the trained learner on test split.
				double correctCounter = 0d;
				for (Instance instance : testInstances) {
					if (this.learner.classifyInstance(instance) == instance.classValue()) {
						correctCounter++;
					}
				}
				this.yValues[i] = correctCounter / testInstances.size();
				this.logger.debug("Training finished. Observed learning curve value (accuracy) of {}.", this.yValues[i]);
			}
			this.logger.info("Computed accuracies of {} for anchor points {}. Now extrapolating a curve from these observations.", Arrays.toString(this.yValues), Arrays.toString(this.anchorPoints));
			return this.extrapolationMethod.extrapolateLearningCurveFromAnchorPoints(this.anchorPoints, this.yValues, this.dataset.size());
		} catch (UnsupportedAttributeTypeException e) {
			throw new AlgorithmException(e, "Error during convertion of the dataset to WEKA instances");
		} catch (AlgorithmExecutionCanceledException | TimeoutException | AlgorithmException e) {
			throw new AlgorithmException(e, "Error during creation of the subsamples for the anchorpoints");
		} catch (ExecutionException e) {
			throw new AlgorithmException(e, "Error during learning curve extrapolation");
		} catch (InvalidAnchorPointsException | InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new AlgorithmException(e, "Error during training/testing the classifier");
		}

	}

	private void createSplit(final double trainsplit, final long seed) {
		long start = System.currentTimeMillis();
		this.logger.debug("Creating split with training portion {} and seed {}", trainsplit, seed);
		this.train = this.dataset.createEmpty();
		this.test = this.dataset.createEmpty();
		IDataset<I> data = this.dataset.createEmpty();
		data.addAll(this.dataset);

		// Shuffle the data
		Random r = new Random(seed);
		Collections.shuffle(data, r);

		// Stratify the data by class
		Map<Object, IDataset<I>> classStrati = new HashMap<>();
		this.dataset.forEach(d -> {
			Object c = d.getTargetValue(Object.class).getValue();
			if (!classStrati.containsKey(c)) {
				classStrati.put(c, this.dataset.createEmpty());
			}
			classStrati.get(c).add(d);
		});

		// Retrieve strati sizes
		Map<Object, Integer> classStratiSizes = new HashMap<>(classStrati.size());
		for (Entry<Object, IDataset<I>> entry : classStrati.entrySet()) {
			classStratiSizes.put(entry.getKey(), classStrati.get(entry.getKey()).size());
		}

		// First assign one item of each class to train and test
		for (Entry<Object, IDataset<I>> entry : classStrati.entrySet()) {
			IDataset<I> availableInstances = classStrati.get(entry.getKey());
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
		for (Entry<Object, IDataset<I>> entry : classStrati.entrySet()) {
			IDataset<I> availableInstances = classStrati.get(entry.getKey());
			int trainItems = (int) Math.min(availableInstances.size(), Math.ceil(trainsplit * classStratiSizes.get(entry.getKey())));
			for (int j = 0; j < trainItems; j++) {
				this.train.add(availableInstances.get(0));
				availableInstances.remove(0);
			}
			int testItems = (int) Math.min(availableInstances.size(), Math.ceil((1 - trainsplit) * classStratiSizes.get(entry.getKey())));
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

	public Classifier getLearner() {
		return this.learner;
	}

	public IDataset<I> getDataset() {
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
