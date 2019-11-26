package ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling;

import java.lang.reflect.Array;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.algorithm.events.AlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.core.dataset.DatasetDeriver;
import ai.libs.jaicore.ml.core.filter.sampling.SampleElementAddedEvent;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.SimpleRandomSampling;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.WaitForSamplingStepEvent;

/**
 * Implementation of Stratified Sampling: Divide dataset into strati and sample
 * from each of these.
 *
 * @author Lukas Brandt
 */
public class StratifiedSampling<D extends IDataset<?>> extends ASamplingAlgorithm<D> {

	private Logger logger = LoggerFactory.getLogger(StratifiedSampling.class);
	private IStratiAmountSelector stratiAmountSelector;
	private IStratiAssigner stratiAssigner;
	private Random random;
	private DatasetDeriver<D>[] stratiBuilder = null;
	private D datasetCopy;
	private boolean allDatapointsAssigned = false;
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
	 */
	public StratifiedSampling(final IStratiAmountSelector stratiAmountSelector, final IStratiAssigner stratiAssigner, final Random random, final D input) {
		super(input);
		this.stratiAmountSelector = stratiAmountSelector;
		this.stratiAssigner = stratiAssigner;
		this.random = random;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmException {
		switch (this.getState()) {
		case CREATED:
			try {
				this.sample = (D) this.getInput().createEmptyCopy();
				if (!this.allDatapointsAssigned) {
					this.datasetCopy = (D) this.getInput().createCopy();
					this.stratiAmountSelector.setNumCPUs(this.getNumCPUs());
					this.stratiAssigner.setNumCPUs(this.getNumCPUs());
					this.stratiBuilder= (DatasetDeriver<D>[])Array.newInstance(new DatasetDeriver<>(this.getInput()).getClass(), this.stratiAmountSelector.selectStratiAmount(this.datasetCopy));

					for (int i = 0; i < this.stratiBuilder.length; i++) {
						this.stratiBuilder[i] = new DatasetDeriver<>(this.getInput());
					}
					this.stratiAssigner.init(this.datasetCopy, this.stratiBuilder.length);
				}
				this.simpleRandomSamplingStarted = false;
			} catch (DatasetCreationException e) {
				throw new AlgorithmException("Could not create a copy of the dataset.", e);
			}
			return this.activate();
		case ACTIVE:
			if (this.sample.size() < this.sampleSize) {
				if (!this.allDatapointsAssigned) {

					/* Stratify the datapoints one by one. */
					IInstance datapoint = this.datasetCopy.remove(0);
					int assignedStrati = this.stratiAssigner.assignToStrati(datapoint);
					if (assignedStrati < 0 || assignedStrati >= this.stratiBuilder.length) {
						throw new AlgorithmException("No existing strati for index " + assignedStrati);
					} else {
						this.stratiBuilder[assignedStrati].add(datapoint);
					}
					if (this.datasetCopy.isEmpty()) {
						this.allDatapointsAssigned = true;
					}
					return new SampleElementAddedEvent(this.getId());
				} else {
					if (!this.simpleRandomSamplingStarted) {

						/* Simple Random Sampling has not started yet -> Initialize one sampling thread per stratum. */
						try {
							this.startSimpleRandomSamplingForStrati();
						} catch (DatasetCreationException e) {
							throw new AlgorithmException("Could not create sample from strati.", e);
						}
						this.simpleRandomSamplingStarted = true;
						return new WaitForSamplingStepEvent(this.getId());
					} else {

						/* Check if all threads are finished. If yes finish Stratified Sampling, wait shortly in this step otherwise. */
						return this.terminate();
					}
				}
			} else {
				return this.terminate();
			}
		case INACTIVE:
			if (this.sample.size() < this.sampleSize) {
				throw new AlgorithmException("Expected sample size was not reached before termination");
			} else {
				return this.terminate();
			}
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
	}

	/**
	 * Calculates the necessary sample sizes and start a Simple Random Sampling
	 * Thread for each stratum.
	 * @throws DatasetCreationException
	 * @throws InterruptedException
	 */
	private void startSimpleRandomSamplingForStrati() throws InterruptedException, DatasetCreationException {
		// Calculate the amount of datapoints that will be used from each strati
		int[] sampleSizeForStrati = new int[this.stratiBuilder.length];
		// Calculate for each stratum the sample size by StratiSize / DatasetSize
		for (int i = 0; i < this.stratiBuilder.length; i++) {
			sampleSizeForStrati[i] = Math.round((float) (this.sampleSize * (this.stratiBuilder[i].currentSizeOfTarget() / (double) this.getInput().size())));
		}

		// Start a Simple Random Sampling thread for each stratum
		DatasetDeriver<D> sampleDeriver = new DatasetDeriver<>(this.getInput());
		for (int i = 0; i < this.stratiBuilder.length; i++) {

			SimpleRandomSampling<D> simpleRandomSampling = new SimpleRandomSampling<>(this.random, this.stratiBuilder[i].build());
			simpleRandomSampling.setSampleSize(sampleSizeForStrati[i]);
			try {
				sampleDeriver.addAll(simpleRandomSampling.call());
			} catch (Exception e) {
				this.logger.error("Unexpected exception during simple random sampling!", e);
			}
		}
		this.sample = sampleDeriver.build();
	}
}
