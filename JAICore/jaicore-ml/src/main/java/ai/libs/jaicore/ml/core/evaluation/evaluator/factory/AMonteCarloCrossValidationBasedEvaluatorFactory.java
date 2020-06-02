package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import java.util.Random;

import org.api4.java.ai.ml.core.IDataConfigurable;
import org.api4.java.ai.ml.core.dataset.splitter.IDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPredictionPerformanceMetricConfigurable;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.common.control.IRandomConfigurable;

/**
 * An abstract factory for configuring Monte Carlo cross-validation based evaluators.
 *
 * @author mwever
 * @author fmohr
 *
 */
public abstract class AMonteCarloCrossValidationBasedEvaluatorFactory<F extends AMonteCarloCrossValidationBasedEvaluatorFactory<F>> implements
		ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>, IRandomConfigurable, IDataConfigurable<ILabeledDataset<? extends ILabeledInstance>>, IPredictionPerformanceMetricConfigurable {

	private IDatasetSplitter<? extends ILabeledDataset<?>> datasetSplitter;
	protected Random random;
	protected int numMCIterations;
	protected ILabeledDataset<?> data;
	private double trainFoldSize;
	private int timeoutForSolutionEvaluation;
	protected IDeterministicPredictionPerformanceMeasure<?, ?> metric;
	private boolean cacheSplitSets = false;

	/**
	 * Standard c'tor.
	 */
	protected AMonteCarloCrossValidationBasedEvaluatorFactory() {
		super();
	}

	/**
	 * Getter for the dataset splitter.
	 * @return Returns the dataset spliiter.
	 */
	public IDatasetSplitter<? extends ILabeledDataset<?>> getDatasetSplitter() {
		return this.datasetSplitter;
	}

	/**
	 * Getter for the number of iterations, i.e. the number of splits considered.
	 * @return The number of iterations.
	 */
	public int getNumMCIterations() {
		return this.numMCIterations;
	}

	/**
	 * Getter for the dataset which is used for splitting.
	 * @return The original dataset that is being split.
	 */
	@Override
	public ILabeledDataset<?> getData() {
		return this.data;
	}

	/**
	 * Getter for the size of the train fold.
	 * @return The portion of the training data.
	 */
	public double getTrainFoldSize() {
		return this.trainFoldSize;
	}

	/**
	 * Getter for the timeout for evaluating a solution.
	 * @return The timeout for evaluating a solution.
	 */
	public int getTimeoutForSolutionEvaluation() {
		return this.timeoutForSolutionEvaluation;
	}

	/**
	 * Configures the evaluator to use the given dataset splitter.
	 * @param datasetSplitter The dataset splitter to be used.
	 * @return The factory object.
	 */
	public F withDatasetSplitter(final IDatasetSplitter<? extends ILabeledDataset<?>> datasetSplitter) {
		this.datasetSplitter = datasetSplitter;
		return this.getSelf();
	}

	public F withRandom(final Random random) {
		this.random = random;
		return this.getSelf();
	}

	/**
	 * Configures the number of monte carlo cross-validation iterations.
	 * @param numMCIterations The number of iterations to run.
	 * @return The factory object.
	 */
	public F withNumMCIterations(final int numMCIterations) {
		this.numMCIterations = numMCIterations;
		return this.getSelf();
	}

	/**
	 * Configures the dataset which is split into train and test data.
	 * @param data The dataset to be split.
	 * @return The factory object.
	 */
	public F withData(final ILabeledDataset<?> data) {
		this.data = data;
		return this.getSelf();
	}

	/**
	 * Configures the portion of the training data relative to the entire dataset size.
	 * @param trainFoldSize The size of the training fold (0,1).
	 * @return The factory object.
	 */
	public F withTrainFoldSize(final double trainFoldSize) {
		this.trainFoldSize = trainFoldSize;
		return this.getSelf();
	}

	/**
	 * Configures a timeout for evaluating a solution.
	 * @param timeoutForSolutionEvaluation The timeout for evaluating a solution.
	 * @return The factory object.
	 */
	public F withTimeoutForSolutionEvaluation(final int timeoutForSolutionEvaluation) {
		this.timeoutForSolutionEvaluation = timeoutForSolutionEvaluation;
		return this.getSelf();
	}

	public abstract F getSelf();

	@Override
	public void setMeasure(final IDeterministicPredictionPerformanceMeasure<?, ?> measure) {
		this.metric = measure;
	}

	@Override
	public void setData(final ILabeledDataset<? extends ILabeledInstance> data) {
		this.withData(data);
	}

	@Override
	public void setRandom(final Random random) {
		this.withRandom(random);

	}

	public F withMeasure(final IDeterministicPredictionPerformanceMeasure<?, ?> measure) {
		this.setMeasure(measure);
		return this.getSelf();
	}
	
	public F withCacheSplitSets(boolean cacheSplitSets) {
		this.cacheSplitSets = cacheSplitSets;
		return this.getSelf();
	}
	
	public boolean getCacheSplitSets() {
		return this.cacheSplitSets;
	}
}
