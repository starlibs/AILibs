package jaicore.ml.core.dataset.sampling.infiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import jaicore.basic.TempFileHandler;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.dataset.ArffUtilities;
import jaicore.ml.core.dataset.sampling.SampleElementAddedEvent;

/**
 * File-level implementation of Systematic Sampling: Sort datapoints and pick
 * every k-th datapoint for the sample.
 * 
 * @author Lukas Brandt
 */
public class SystematicFileSampling extends AFileSamplingAlgorithm {

	private Random random;
	private int k;
	private int startIndex;
	private int index;
	private int addedDatapoints;
	private int datapointAmount;
	private TempFileHandler tempFileHandler;
	private DatasetFileSorter sorter;
	private Comparator<String> datapointComparator;
	private File sortedDatasetFile;
	private BufferedReader sortedDatasetFileReader;

	/**
	 * Simple constructor that uses the default datapoint comparator.
	 * 
	 * @param random          Random Object for determining the sampling start
	 *                        point.
	 * @param tempFileHandler Handler for temporary files, which are needed for
	 *                        sorting the original file.
	 */
	public SystematicFileSampling(Random random, TempFileHandler tempFileHandler) {
		this(random, tempFileHandler, null);
		
	}

	/**
	 * Constructor for a custom datapoint comparator.
	 * 
	 * @param random              Random Object for determining the sampling start
	 *                            point.
	 * @param tempFileHandler     Handler for temporary files, which are needed for
	 *                            sorting the original file.
	 * @param datapointComparator Comparator to sort the dataset.
	 */
	public SystematicFileSampling(Random random, TempFileHandler tempFileHandler,
			Comparator<String> datapointComparator) {
		this.random = random;
		this.tempFileHandler = tempFileHandler;
		this.datapointComparator = datapointComparator;
	}

	@Override
	public AlgorithmEvent nextWithException()
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		switch (this.getState()) {
		case created:
			// Sort dataset and skip with reader the ARFF header.
			try {
				this.sorter = new DatasetFileSorter(this.getInput(), this.tempFileHandler);
				if (this.datapointComparator != null) {
					this.sorter.setComparator(this.datapointComparator);
				}
				this.sortedDatasetFile = this.sorter.sort(this.tempFileHandler.getTempFileDirPath() + File.separator + UUID.randomUUID().toString());
				this.sortedDatasetFileReader = new BufferedReader(new FileReader(this.sortedDatasetFile));
				ArffUtilities.skipWithReaderToDatapoints(this.sortedDatasetFileReader);
			} catch (IOException e) {
				throw new AlgorithmException(e, "Was not able to create a sorted dataset file.");
			}
			// Count datapoints in the sorted dataset and initialize variables.
			try {
				this.datapointAmount = ArffUtilities.countDatasetEntries(this.sortedDatasetFile, true);
				this.k = (int) Math.floor(this.datapointAmount / this.sampleSize);
				this.addedDatapoints = 0;
				this.startIndex = this.random.nextInt(this.datapointAmount);
				this.index = 0;
				while(this.index < this.startIndex) {
					this.sortedDatasetFileReader.readLine();
					this.index++;
				}
				return this.activate();
			} catch (IOException e) {
				throw new AlgorithmException(e, "Was not able to count the datapoints.");
			}
		case active:
			// If the sample size is not reached yet, add the next datapoint from the
			// systematic sampling method.
			if (this.addedDatapoints < this.sampleSize) {
				try {
					// Determine the next k-th element.
					int e = (this.startIndex + (this.addedDatapoints) * this.k) % this.datapointAmount;
					String datapoint = this.sortedDatasetFileReader.readLine();
					// Last datapoint of the file reached -> Start from the file beginning again.
					if (this.index > e) {
						this.sortedDatasetFileReader.close();
						this.sortedDatasetFileReader = new BufferedReader(new FileReader(this.sortedDatasetFile));
						ArffUtilities.skipWithReaderToDatapoints(this.sortedDatasetFileReader);
						datapoint = this.sortedDatasetFileReader.readLine();
						this.index = 1;
					}
					// Skip to the next k-th datapoint.
					while(this.index < e) {
						datapoint = this.sortedDatasetFileReader.readLine();
						this.index++;
					}
					// Add this datapoint to the output file.
					this.outputFileWriter.write(datapoint + "\n");
					this.addedDatapoints++;
					return new SampleElementAddedEvent(getId());
				} catch (IOException e) {
					throw new AlgorithmException(e, "Was not able to read from sorted dataset file.");
				}
			} else {
				// Delete sorted dataset file and terminate
				this.sortedDatasetFile.delete();
				return this.terminate();
			}
		case inactive: {
			if (this.addedDatapoints < this.sampleSize) {
				throw new AlgorithmException("Expected sample size was not reached before termination");
			} else {
				return this.terminate();
			}
		}
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
	}
	
}
