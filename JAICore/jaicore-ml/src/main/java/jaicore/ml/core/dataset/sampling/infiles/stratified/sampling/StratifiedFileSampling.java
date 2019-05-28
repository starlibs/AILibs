package jaicore.ml.core.dataset.sampling.infiles.stratified.sampling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.TempFileHandler;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.dataset.ArffUtilities;
import jaicore.ml.core.dataset.sampling.SampleElementAddedEvent;
import jaicore.ml.core.dataset.sampling.infiles.AFileSamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.infiles.ReservoirSampling;
import jaicore.ml.core.dataset.sampling.inmemory.WaitForSamplingStepEvent;

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
	public StratifiedFileSampling(Random random, IStratiFileAssigner stratiFileAssigner, File input) {
		super(input);
		this.random = random;
		this.assigner = stratiFileAssigner;
		this.tempFileHandler = new TempFileHandler();
	}

	@Override
	public AlgorithmEvent nextWithException()
			throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
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
				ArffUtilities.skipWithReaderToDatapoints(reader);
				return this.activate();
			} catch (IOException e) {
				throw new AlgorithmException(e, "Was not able to count the datapoints.");
			}
		case ACTIVE:
			if (this.streamedDatapoints < this.datapointAmount) {
				try {
					// Assign each datapoint to a stratum.
					String datapoint = reader.readLine();
					if (datapoint != null && datapoint.trim().length() > 0 && datapoint.trim().charAt(0) != '%') {
						this.assigner.assignDatapoint(datapoint);
					}
					this.streamedDatapoints++;
					return new SampleElementAddedEvent(getId());
				} catch (IOException e) {
					throw new AlgorithmException(e, "Was not able to read datapoint line form input file");
				}
			} else {
				try {
					this.reader.close();
				} catch (IOException e) {
					throw new AlgorithmException(e, "Was not able to close input file reader.");
				}
				if (!stratiSamplingStarted) {
					// Start Reservoir Sampling inside the strati.
					this.stratiSamplingStarted = true;
					this.startReservoirSamplingForStrati(this.assigner.getAllCreatedStrati());
					return new WaitForSamplingStepEvent(getId());
				} else {
					if (!this.stratiSamplingFinished) {
						// Check if all threads for sampling inside the strati are finished. If no, wait
						// shortly in this step.
						if (this.executorService.isTerminated()) {
							this.stratiSamplingFinished = true;
						} else {
							Thread.sleep(100);
						}
						return new WaitForSamplingStepEvent(getId());
					} else {
						// Write strati sampling results to the outputand terminate.
						try {
							for (int i = 0; i < this.sample.size(); i++) {
								this.outputFileWriter.write(sample.get(i) + "\n");
							}
							return this.terminate();
						} catch (IOException e) {
							throw new AlgorithmException(e, "Was not able to write datapoint into output file.");
						}

					}
				}
			}
		case INACTIVE:
			if (this.streamedDatapoints < this.datapointAmount || !this.stratiSamplingStarted
					|| !this.stratiSamplingFinished) {
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
	private void startReservoirSamplingForStrati(Map<String, Integer> strati) {
		// Calculate the amount of datapoints that will be used from each strati
		int[] sampleSizeForStrati = new int[strati.keySet().size()];
		// Calculate for each stratum the sample size by StratiSize / DatasetSize
		int i = 0;
		for (Entry<String, Integer> entry : strati.entrySet()) {
			sampleSizeForStrati[i] = Math
					.round((float) (this.sampleSize * ((double) strati.get(entry.getKey()) / (double) this.datapointAmount)));
			i++;
		}

		// Start a Reservoir Sampling thread for each stratum
		i = 0;
		for (Entry<String, Integer> entry : strati.entrySet()) {
			int index = i;
			this.executorService.execute(() -> {
				String outputFile = tempFileHandler.createTempFile();
				ReservoirSampling reservoirSampling = new ReservoirSampling(random,
						tempFileHandler.getTempFile(entry.getKey()));
				reservoirSampling.setSampleSize(sampleSizeForStrati[index]);
				try {
					reservoirSampling.setOutputFileName(tempFileHandler.getTempFile(outputFile).getAbsolutePath());
					reservoirSampling.call();
					BufferedReader bufferedReader = tempFileHandler.getFileReaderForTempFile(outputFile);
					ArffUtilities.skipWithReaderToDatapoints(bufferedReader);
					String line;
					while ((line = bufferedReader.readLine()) != null) {
						if (!(line.trim().equals("") || line.trim().charAt(0) == '%')) {
							synchronized (sample) {
								sample.add(line);
							}
						}
					}
				} catch (Exception e) {
					logger.error("Unexpected exception during reservoir sampling!", e);
				}
			});
			i++;
		}
		// Prevent executor service from more threads being added.
		this.executorService.shutdown();
	}

}
