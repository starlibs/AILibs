package jaicore.ml.core.dataset.sampling.stratified.sampling;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.SampleElementAddedEvent;
import jaicore.ml.core.dataset.sampling.SimpleRandomSampling;
import jaicore.ml.core.dataset.sampling.WaitForSamplingStepEvent;

/**
 * Implementation of Stratified Sampling: Divide dataset into strati and sample
 * from each of these.
 * 
 * @author Lukas Brandt
 */
public class StratifiedSampling extends ASamplingAlgorithm {

	private IStratiAmountSelector stratiAmountSelector;
	private IStratiAssigner stratiAssigner;
	private Random random;
	private IDataset[] strati;
	private IDataset datasetCopy;
	private ExecutorService executorService;
	private boolean considerStandardDeviation;
	private boolean simpleRandomSamplingStarted;

	/**
	 * Constructor for Stratified Sampling.
	 * 
	 * @param stratiAmountSelector
	 *            The custom selector for the used amount of strati.
	 * @param stratiAssigner
	 *            Custom logic to assign datapoints into strati.
	 * @param random
	 *            Random object for sampling inside of the strati.
	 * @param considerStandardDeviation
	 *            Flag if the overall sample should be composed from each strati
	 *            partial to StratiSize / DatasetSize or if all strati whose size is
	 *            inside of AverageStratiSize +/- StandardDeviationOfStratiSize
	 *            should be used uniformly distributed.
	 */
	public StratifiedSampling(IStratiAmountSelector stratiAmountSelector, IStratiAssigner stratiAssigner, Random random,
			boolean considerStandardDeviation) {
		this.stratiAmountSelector = stratiAmountSelector;
		this.stratiAssigner = stratiAssigner;
		this.random = random;
		this.considerStandardDeviation = considerStandardDeviation;
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (this.getState()) {
		case created:
			this.sample = this.createEmptyDatasetFromInputSchema();
			this.datasetCopy = this.createEmptyDatasetFromInputSchema();
			this.datasetCopy.addAll(this.getInput());
			this.stratiAmountSelector.setNumCPUs(this.getNumCPUs());
			this.stratiAssigner.setNumCPUs(this.getNumCPUs());
			this.strati = new IDataset[this.stratiAmountSelector.selectStratiAmount(this.datasetCopy)];
			for (int i = 0; i < this.strati.length; i++) {
				this.strati[i] = this.createEmptyDatasetFromInputSchema();
			}
			this.simpleRandomSamplingStarted = false;
			this.stratiAssigner.init(this.datasetCopy, this.strati.length);
			this.setState(AlgorithmState.active);
			this.executorService = Executors.newCachedThreadPool();
			return new AlgorithmInitializedEvent();
		case active:
			if (this.sample.size() < this.sampleSize) {
				if (this.datasetCopy.size() >= 1) {
					// Stratify the datapoints one by one.
					IInstance datapoint = (IInstance) this.datasetCopy.remove(0);
					int assignedStrati = this.stratiAssigner.assignToStrati(datapoint);
					if (assignedStrati < 0 || assignedStrati >= this.strati.length) {
						throw new Exception("No existing strati for index " + assignedStrati);
					} else {
						this.strati[assignedStrati].add(datapoint);
					}
					return new SampleElementAddedEvent();
				} else {
					if (!simpleRandomSamplingStarted) {
						// Simple Random Sampling has not started yet -> Initialize one sampling thread
						// per stratum.
						this.startSimpleRandomSamplingForStrati();
						this.simpleRandomSamplingStarted = true;
						return new WaitForSamplingStepEvent();
					} else {
						// Check if all threads are finished. If yes finish Stratified Sampling, wait
						// shortly in this step otherwise.
						if (this.executorService.isTerminated()) {
							this.setState(AlgorithmState.inactive);
							return new AlgorithmFinishedEvent();
						} else {
							synchronized (Thread.currentThread()) {
								Thread.currentThread().wait(100);
								return new WaitForSamplingStepEvent();
							}
						}
					}
				}
			} else {
				this.setState(AlgorithmState.inactive);
				return new AlgorithmFinishedEvent();
			}
		case inactive: {
			if (this.sample.size() < this.sampleSize) {
				throw new Exception("Expected sample size was not reached before termination");
			} else {
				return new AlgorithmFinishedEvent();
			}
		}
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
	}

	/**
	 * Calculates the necessary sample sizes and start a Simple Random Sampling
	 * Thread for each stratum.
	 */
	private void startSimpleRandomSamplingForStrati() {
		// Calculate the amount of datapoints that will be used from each strati
		int[] sampleSizeForStrati = new int[this.strati.length];
		if (this.considerStandardDeviation) {
			// Calculate Mean and StandardDeviation.
			Mean mean = new Mean();
			StandardDeviation standardDeviation = new StandardDeviation();
			for (int i = 0; i < this.strati.length; i++) {
				mean.increment(this.strati[i].size());
				standardDeviation.increment(this.strati[i].size());
			}
			// Check which strati are inside of Mean +/- StandardDeviation
			double lowerBound = mean.getResult() - standardDeviation.getResult();
			double upperBound = mean.getResult() + standardDeviation.getResult();
			int numberOfStratiInsideOfInterval = 0;
			int combinedSizeOfStratiInsideOfInterval = 0;
			for (int i = 0; i < this.strati.length; i++) {
				if (this.strati[i].size() < lowerBound || this.strati[i].size() > upperBound) {
					// Outside of the interval -> Calculate ratio.
					sampleSizeForStrati[i] = (int) (this.sampleSize
							* ((double) this.strati[i].size() / (double) this.getInput().size()));
				} else {
					// Inside of interval -> Mark for uniform distribution.
					sampleSizeForStrati[i] = -1;
					numberOfStratiInsideOfInterval++;
					combinedSizeOfStratiInsideOfInterval += this.strati[i].size();
				}
			}
			// Assign uniformly distributed sample sizes to marked strati.
			int sizeForStratiInsideOfInterval = (int) (this.sampleSize
					* ((double) combinedSizeOfStratiInsideOfInterval / (double) this.getInput().size())
					/ (double) numberOfStratiInsideOfInterval);
			for (int i = 0; i < this.strati.length; i++) {
				if (sampleSizeForStrati[i] == -1) {
					sampleSizeForStrati[i] = sizeForStratiInsideOfInterval;
				}
			}
		} else {
			// Calculate for each stratum the sample size by StratiSize / DatasetSize
			for (int i = 0; i < this.strati.length; i++) {
				sampleSizeForStrati[i] = (int) (this.sampleSize
						* ((double) this.strati[i].size() / (double) this.getInput().size()));
				System.out.println("Strati size: " + this.strati[i].size() + " sample amount " + sampleSizeForStrati[i]);
			}
		}

		// Start a Simple Random Sampling thread for each stratum
		for (int i = 0; i < this.strati.length; i++) {
			int index = i;
			this.executorService.execute(new Runnable() {
				@Override
				public void run() {
					SimpleRandomSampling simpleRandomSampling = new SimpleRandomSampling(random);
					simpleRandomSampling.setInput(strati[index]);
					simpleRandomSampling.setSampleSize(sampleSizeForStrati[index]);
					try {
						sample.addAll(simpleRandomSampling.call());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		// Prevent executor service from more threads being added.
		this.executorService.shutdown();
	}

}
