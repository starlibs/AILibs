package ai.libs.jaicore.ml.core.filter.sampling.infiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.basic.TempFileHandler;
import ai.libs.jaicore.ml.core.filter.sampling.SampleElementAddedEvent;
import ai.libs.jaicore.timing.TimedComputation;

/**
 * File-level implementation of Systematic Sampling: Sort datapoints and pick
 * every k-th datapoint for the sample.
 *
 * @author Lukas Brandt
 */
public class SystematicFileSampling extends AFileSamplingAlgorithm {

	private Random random;
	private int index;
	private int addedDatapoints;
	private TempFileHandler tempFileHandler;
	private Comparator<String> datapointComparator;
	private BufferedReader sortedDatasetFileReader;
	private List<Integer> indicesForSelection;
	private DatasetFileSorter sorter; // this is an object variable in order to be cancelable

	/**
	 * Simple constructor that uses the default datapoint comparator.
	 *
	 * @param random
	 *            Random Object for determining the sampling start point.
	 */
	public SystematicFileSampling(final Random random, final File input) {
		this(random, null, input);
	}

	/**
	 * Constructor for a custom datapoint comparator.
	 *
	 * @param random
	 *            Random Object for determining the sampling start point.
	 * @param datapointComparator
	 *            Comparator to sort the dataset.
	 */
	public SystematicFileSampling(final Random random, final Comparator<String> datapointComparator, final File input) {
		super(input);
		this.random = random;
		this.datapointComparator = datapointComparator;
		this.tempFileHandler = new TempFileHandler();
	}

	@Override
	public IAlgorithmEvent nextWithException()
			throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmTimeoutedException {
		switch (this.getState()) {
		case CREATED:
			// Sort dataset and skip with reader the ARFF header.
			File sortedDatasetFile = null;
			try {
				this.sorter = new DatasetFileSorter(this.getInput(), this.tempFileHandler);
				if (this.datapointComparator != null) {
					this.sorter.setComparator(this.datapointComparator);
				}
				this.setDeadline();
				long remainingMS = this.getRemainingTimeToDeadline().milliseconds() - this.getTimeoutPrecautionOffset();
				sortedDatasetFile = TimedComputation.compute(() -> this.sorter.sort(
						this.tempFileHandler.getTempFileDirPath() + File.separator + UUID.randomUUID().toString()), remainingMS, "No time left");
				sortedDatasetFile.deleteOnExit();
				this.sortedDatasetFileReader = new BufferedReader(new FileReader(sortedDatasetFile));
				ArffUtilities.skipWithReaderToDatapoints(this.sortedDatasetFileReader);
			} catch (IOException | ExecutionException e) {
				if (e.getCause() instanceof AlgorithmExecutionCanceledException) {
					throw (AlgorithmExecutionCanceledException)e.getCause();
				}
				throw new AlgorithmException("Was not able to create a sorted dataset file.", e);
			}
			// Count datapoints in the sorted dataset and initialize variables.
			try {
				this.addedDatapoints = 0;
				this.index = 0;
				int datapointAmount = ArffUtilities.countDatasetEntries(sortedDatasetFile, true);
				this.indicesForSelection = new LinkedList<>();
				int k = datapointAmount / this.sampleSize;
				int startIndex = this.random.nextInt(datapointAmount);
				int i = 0;
				while (this.indicesForSelection.size() < this.sampleSize) {
					if (i % 100 == 0) {
						this.checkAndConductTermination();
					}
					int e = (startIndex + k * (i++)) % datapointAmount;
					this.indicesForSelection.add(e);
				}
				this.indicesForSelection.sort(Integer::compare);
				return this.activate();
			} catch (IOException e) {
				throw new AlgorithmException("Was not able to count the datapoints.", e);
			}
		case ACTIVE:
			// If the sample size is not reached yet, add the next datapoint from the
			// systematic sampling method.
			if (this.addedDatapoints < this.sampleSize) {
				try {
					if (this.addedDatapoints % 100 == 0) {
						this.checkAndConductTermination();
					}

					// Determine and find the next k-th element.
					int e = this.indicesForSelection.get(this.addedDatapoints);
					String datapoint = this.sortedDatasetFileReader.readLine();
					this.index++;
					while (this.index < e) {
						if (this.index % 100 == 0) {
							this.checkAndConductTermination();
						}
						datapoint = this.sortedDatasetFileReader.readLine();
						this.index++;
					}
					// Add this datapoint to the output file.
					assert datapoint != null;
					this.outputFileWriter.write(datapoint + "\n");
					this.addedDatapoints++;
					return new SampleElementAddedEvent(this);
				} catch (IOException e) {
					throw new AlgorithmException("Was not able to read from sorted dataset file.", e);
				}
			} else {
				// Delete sorted dataset file and terminate
				this.cleanUp();
				return this.terminate();
			}
		case INACTIVE:
			this.cleanUp();
			if (this.addedDatapoints < this.sampleSize) {
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
	public void cancel() {
		super.cancel();
		this.sorter.cancel();
	}

	@Override
	protected void cleanUp() {
		this.tempFileHandler.cleanUp();
	}

}
