package ai.libs.jaicore.ml.core.filter.sampling.infiles.stratified.sampling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.TempFileHandler;
import ai.libs.jaicore.ml.core.filter.sampling.SampleElementAddedEvent;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.AFileSamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.ArffUtilities;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.ReservoirSampling;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.WaitForSamplingStepEvent;

public class StratifiedFileSampling extends AFileSamplingAlgorithm {

	private Logger logger = LoggerFactory.getLogger(StratifiedFileSampling.class);
	private Random random;
	private TempFileHandler tempFileHandler;
	private BufferedReader reader;
	private IStratiFileAssigner assigner;
	private int datapointAmount;
	private int streamedDatapoints;
	private boolean stratiSamplingStarted;
	private boolean stratiSamplingFinished;
	private ExecutorService executorService;
	private List<String> sample;

	/**
	 * Constructor for a Stratified File Sampler.
	 *
	 * @param random
	 *            Random object for sampling inside of the strati.
	 * @param stratiFileAssigner
	 *            Assigner for datapoints to strati.
	 */
	public StratifiedFileSampling(final Random random, final IStratiFileAssigner stratiFileAssigner, final File input) {
		super(input);
		this.random = random;
		this.assigner = stratiFileAssigner;
		this.tempFileHandler = new TempFileHandler();
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmTimeoutedException {
		switch (this.getState()) {
		case CREATED:
			// Initialize variables.
			try {
				this.assigner.setArffHeader(ArffUtilities.extractArffHeader(this.getInput()));
				this.assigner.setTempFileHandler(this.tempFileHandler);
				this.datapointAmount = ArffUtilities.countDatasetEntries(this.getInput(), true);
				this.streamedDatapoints = 0;
				this.stratiSamplingStarted = false;
				this.stratiSamplingFinished = false;
				this.sample = new LinkedList<>();
				this.reader = new BufferedReader(new FileReader(this.getInput()));
				this.executorService = Executors.newCachedThreadPool();
				ArffUtilities.skipWithReaderToDatapoints(this.reader);
				return this.activate();
			} catch (IOException e) {
				throw new AlgorithmException("Was not able to count the datapoints.", e);
			}
		case ACTIVE:
			if (this.streamedDatapoints % 100 == 0) {
				this.checkAndConductTermination();
			}
			if (this.streamedDatapoints < this.datapointAmount) {
				try {
					// Assign each datapoint to a stratum.
					String datapoint = this.reader.readLine();
					if (datapoint != null && datapoint.trim().length() > 0 && datapoint.trim().charAt(0) != '%') {
						this.assigner.assignDatapoint(datapoint);
					}
					this.streamedDatapoints++;
					return new SampleElementAddedEvent(this);
				} catch (IOException e) {
					throw new AlgorithmException("Was not able to read datapoint line form input file", e);
				}
			} else {
				this.logger.debug("All datapoints are assigned, now sampling from strati.");
				try {
					this.reader.close();
				} catch (IOException e) {
					throw new AlgorithmException("Was not able to close input file reader.", e);
				}
				if (!this.stratiSamplingStarted) {
					// Start Reservoir Sampling inside the strati.
					this.stratiSamplingStarted = true;
					this.startReservoirSamplingForStrati(this.assigner.getAllCreatedStrati());
					return new WaitForSamplingStepEvent(this);
				} else {
					if (!this.stratiSamplingFinished) {
						// Check if all threads for sampling inside the strati are finished. If no, wait
						// shortly in this step.
						if (this.executorService.isTerminated()) {
							this.stratiSamplingFinished = true;
						} else {
							Thread.sleep(100);
						}
						return new WaitForSamplingStepEvent(this);
					} else {
						// Write strati sampling results to the outputand terminate.
						try {
							if (this.sample.size() != this.sampleSize) {
								throw new IllegalStateException("Will write " + this.sample.size() + " instead of " + this.sampleSize + " instances.");
							}
							for (int i = 0; i < this.sample.size(); i++) {
								if (i % 100 == 0) {
									this.checkAndConductTermination();
								}
								this.outputFileWriter.write(this.sample.get(i) + "\n");
							}
							return this.terminate();
						} catch (IOException e) {
							throw new AlgorithmException("Was not able to write datapoint into output file.", e);
						}

					}
				}
			}
		case INACTIVE:
			if (this.streamedDatapoints < this.datapointAmount || !this.stratiSamplingStarted || !this.stratiSamplingFinished) {
				throw new AlgorithmException("Expected sample size was not reached before termination");
			} else {
				return this.terminate();
			}
		default:
			this.cleanUp();
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
	}

	@Override
	protected void cleanUp() {
		this.executorService.shutdownNow();
		this.tempFileHandler.cleanUp();
	}

	/**
	 * Calculates the necessary sample sizes and start a Simple Random Sampling
	 * Thread for each stratum.
	 */
	private void startReservoirSamplingForStrati(final Map<String, Integer> strati) {
		this.logger.info("Start reservoir sampling for strati.");
		// Calculate the amount of datapoints that will be used from each strati
		int[] sampleSizeForStrati = new int[strati.keySet().size()];
		// Calculate for each stratum the sample size by StratiSize / DatasetSize
		int i = 0;
		int numOfSamplesThatWillBeCreated = 0;
		List<Integer> fillupStrati = new ArrayList<>(); // strati to fill up rounding instances
		for (Entry<String, Integer> entry : strati.entrySet()) {
			sampleSizeForStrati[i] = (int)Math.floor((float) (this.sampleSize * ((double) strati.get(entry.getKey()) / (double) this.datapointAmount)));
			numOfSamplesThatWillBeCreated += sampleSizeForStrati[i];
			fillupStrati.add(i);
			i++;
		}
		while (numOfSamplesThatWillBeCreated < this.sampleSize) {
			Collections.shuffle(fillupStrati, this.random);
			int indexForNextFillUp = fillupStrati.remove(0);
			sampleSizeForStrati[indexForNextFillUp] ++;
			numOfSamplesThatWillBeCreated ++;
		}
		if (numOfSamplesThatWillBeCreated != this.sampleSize) {
			throw new IllegalStateException("The strati sum up to a size of " + numOfSamplesThatWillBeCreated + " instead of " + this.sampleSize + ".");
		}

		// Start a Reservoir Sampling thread for each stratum
		i = 0;
		for (Entry<String, Integer> entry : strati.entrySet()) {
			final int index = i;
			this.executorService.execute(() -> {
				String outputFile = this.tempFileHandler.createTempFile();
				ReservoirSampling reservoirSampling = new ReservoirSampling(this.random, this.tempFileHandler.getTempFile(entry.getKey()));
				reservoirSampling.setSampleSize(sampleSizeForStrati[index]);
				try {
					reservoirSampling.setOutputFileName(this.tempFileHandler.getTempFile(outputFile).getAbsolutePath());
					reservoirSampling.call();
					BufferedReader bufferedReader = this.tempFileHandler.getFileReaderForTempFile(outputFile);
					ArffUtilities.skipWithReaderToDatapoints(bufferedReader);
					String line;
					while ((line = bufferedReader.readLine()) != null) {
						if (!(line.trim().equals("") || line.trim().charAt(0) == '%')) {
							synchronized (this.sample) {
								this.sample.add(line);
							}
						}
					}
				} catch (Exception e) {
					this.logger.error("Unexpected exception during reservoir sampling!", e);
				}
			});
			i++;
		}

		// Prevent executor service from more threads being added.
		this.executorService.shutdown();
	}

}
