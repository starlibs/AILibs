package jaicore.ml.core.dataset.sampling.stratified.sampling;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
 * Implementation of Stratified Sampling: Divide dataset into strati and sample from each of these.
 * 
 * @author Lukas Brandt
 */
public class StratifiedSampling extends ASamplingAlgorithm{

	private IStratiAmountSelector stratiAmountSelector;
	private IStratiAssigner stratiAssigner;
	private Random random;
	private IDataset[] strati;
	private IDataset datasetCopy;
	private ExecutorService executorService;
	private boolean simpleRandomSamplingStarted;
	
	/**
	 * Constructor for Stratified Sampling.
	 * @param stratiAmountSelector The custom selector for the used amount of strati.
	 * @param stratiAssigner Custom logic to assign datapoints into strati.
	 * @param random Random object for sampling inside of the strati.
	 */
	public StratifiedSampling(IStratiAmountSelector stratiAmountSelector, IStratiAssigner stratiAssigner, Random random) {
		this.stratiAmountSelector = stratiAmountSelector;
		this.stratiAssigner = stratiAssigner;
		this.random = random;
	}
	
	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (this.getState()) {
		case created:
			// TODO: create empty dataset
			this.sample = null;
			// TODO: create empty dataset
			this.datasetCopy = null;
			this.datasetCopy.addAll(this.getInput());
			this.strati = new IDataset[this.stratiAmountSelector.selectStratiAmount(this.datasetCopy)];
			// TODO: create emtpy strati dataset
			for (int i = 0; i < this.strati.length; i++) {
				this.strati[i] = null;
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
					IInstance datapoint = this.datasetCopy.remove(0);
					int assignedStrati = this.stratiAssigner.assignToStrati(datapoint);
					if (assignedStrati < 0 || assignedStrati >= this.strati.length) {
						throw new Exception("No existing strati for index " + assignedStrati);
					} else {
						this.strati[assignedStrati].add(datapoint);
					}
					return new SampleElementAddedEvent();
				} else {
					if (!simpleRandomSamplingStarted) {
						// Simple Random Sampling has not started yet -> Initialize one sampling thread per stratum
						for (int i = 0; i < this.strati.length; i++) {
							int index = i;
							this.executorService.execute(new Runnable() {
								@Override
								public void run() {
									int sizeOfStratiSample = (int)(sampleSize * ((double)strati[index].size() / (double)getInput().size()));
									SimpleRandomSampling simpleRandomSampling = new SimpleRandomSampling(random);
									simpleRandomSampling.setInput(strati[index]);
									simpleRandomSampling.setSampleSize(sizeOfStratiSample);
									try {
										sample.addAll(simpleRandomSampling.call());
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
						}
						// Prevent executor service from more threads being added and set Simple Random Sampling to being started.
						this.executorService.shutdown();
						this.simpleRandomSamplingStarted = true;
						return new WaitForSamplingStepEvent();
					} else {
						// Check if all threads are finished. If yes finish Stratified Sampling, wait shortly in this step otherwise.
						if (this.executorService.isTerminated()) {
							this.setState(AlgorithmState.inactive);
							return new AlgorithmFinishedEvent();
						} else {
							wait(100);
							return new WaitForSamplingStepEvent();
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
			throw new IllegalStateException("Unknown algorithm state "+ this.getState());
		}
	}

}
