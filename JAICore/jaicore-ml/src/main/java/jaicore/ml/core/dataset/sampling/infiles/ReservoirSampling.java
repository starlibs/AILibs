package jaicore.ml.core.dataset.sampling.infiles;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.dataset.ArffUtilities;
import jaicore.ml.core.dataset.sampling.SampleElementAddedEvent;

/**
 * Implementation of the Reservoir Sampling algorithm(comparable to a Simple
 * Random Sampling for streamed data). For a desired sample of size n, the first
 * n elements of the stream are directly selected and the remaining ones will
 * replace these with decreasing probability.
 * 
 * @author Lukas Brandt
 */
public class ReservoirSampling extends AFileSamplingAlgorithm {

	private Random random;
	private BufferedReader reader;
	private int datapointAmount;
	private int streamedDatapoints;
	private String[] sampledDatapoints;

	public ReservoirSampling(Random random) {
		this.random = random;
	}

	@Override
	public AlgorithmEvent nextWithException()
			throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		switch (this.getState()) {
		case created:
			// Initialize variables.
			try {
				this.datapointAmount = ArffUtilities.countDatasetEntries(this.getInput(), true);
				this.streamedDatapoints = 0;
				this.sampledDatapoints = new String[this.sampleSize];
				this.reader = new BufferedReader(new FileReader(this.getInput()));
				ArffUtilities.skipWithReaderToDatapoints(reader);
				return this.activate();
			} catch (IOException e) {
				throw new AlgorithmException(e, "Was not able to count the datapoints.");
			}
		case active:
			if (this.streamedDatapoints < this.datapointAmount) {
				try {
					// Get current datapoint.
					String datapoint = reader.readLine();
					if (datapoint != null && datapoint.trim().length() > 0 && datapoint.trim().charAt(0) != '%') {
						if (this.streamedDatapoints < this.sampleSize) {
							// Take the first n elements directly for the sample.
							this.sampledDatapoints[streamedDatapoints] = datapoint.trim();
						} else {
							// Replace elements with decreasing probability.
							int j = this.random.nextInt(this.streamedDatapoints);
							if (j < this.sampleSize) {
								this.sampledDatapoints[j] = datapoint.trim();
							}
						}
					}
					this.streamedDatapoints++;
					return new SampleElementAddedEvent(getId());
				} catch (IOException e) {
					throw new AlgorithmException(e, "Was not able to read datapoint line from input file");
				}
			} else {
				try {
					this.reader.close();
					// Write sampled datapoints into output file and terminate.
					for (int i = 0; i < this.sampledDatapoints.length; i++) {
						this.outputFileWriter.write(sampledDatapoints[i] + "\n");
					}
					return this.terminate();
				} catch (IOException e) {
					throw new AlgorithmException(e, "Was not able to write sampled datapoints into output files.");
				}
			}
		case inactive: {
			if (this.streamedDatapoints < this.datapointAmount) {
				throw new AlgorithmException("Expected sample size was not reached before termination");
			} else {
				return this.terminate();
			}
		}
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
	}

	@Override
	protected void cleanUp() {
	}

}
