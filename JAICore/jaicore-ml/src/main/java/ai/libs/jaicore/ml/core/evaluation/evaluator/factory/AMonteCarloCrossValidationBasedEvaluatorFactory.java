package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import org.api4.java.ai.ml.core.dataset.splitter.IDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

/**
 * An abstract factory for configuring Monte Carlo cross-validation based evaluators.
 *
 * @author mwever
 * @author fmohr
 *
 */
public abstract class AMonteCarloCrossValidationBasedEvaluatorFactory<I extends ILabeledInstance, D extends ILabeledDataset<I>, F extends AMonteCarloCrossValidationBasedEvaluatorFactory<I, D, F>> implements ISupervisedLearnerEvaluatorFactory<I, D> {

	private IDatasetSplitter<D> datasetSplitter;
	private int seed;
	private int numMCIterations;
	private D data;
	private double trainFoldSize;
	private int timeoutForSolutionEvaluation;

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
	public IDatasetSplitter<D> getDatasetSplitter() {
		return this.datasetSplitter;
	}

	/**
	 * Getter for the random seed.
	 * @return Seed used for generating randomized dataset splits.
	 */
	public int getSeed() {
		return this.seed;
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
	public D getData() {
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
	public F withDatasetSplitter(final IDatasetSplitter<D> datasetSplitter) {
		this.datasetSplitter = datasetSplitter;
		return this.getSelf();
	}

	/**
	 * Configures the evaluator to use the given random seed.
	 * @param seed The seed to be used for pseudo-randomization.
	 * @return The factory object.
	 */
	public F withSeed(final int seed) {
		this.seed = seed;
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
	public F withData(final D data) {
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
}
