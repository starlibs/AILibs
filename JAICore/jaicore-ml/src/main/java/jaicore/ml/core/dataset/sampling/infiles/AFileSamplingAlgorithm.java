package jaicore.ml.core.dataset.sampling.infiles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.ml.core.dataset.ArffUtilities;

/**
 * An abstract class for file-based sampling algorithms providing basic
 * functionality of an algorithm.
 *
 * @author Lukas Brandt
 */
public abstract class AFileSamplingAlgorithm extends AAlgorithm<File, File> {

	private static Logger LOG = LoggerFactory.getLogger(AFileSamplingAlgorithm.class);

	protected Integer sampleSize = null;
	private String outputFilePath = null;
	protected FileWriter outputFileWriter;

	public void setSampleSize(int size) {
		this.sampleSize = size;
	}

	public void setOutputFileName(String outputFilePath) throws IOException {
		this.outputFilePath = outputFilePath;
		outputFileWriter = new FileWriter(outputFilePath);
	}

	@Override
	public File call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		Instant timeoutTime = null;
		if (this.getTimeout().milliseconds() <= 0) {
			LOG.debug("Invalid or no timeout set. There will be no timeout in this algorithm run");
			timeoutTime = Instant.MAX;
		} else {
			timeoutTime = Instant.now().plus(getTimeout().milliseconds(), ChronoUnit.MILLIS);
			LOG.debug("Set timeout to {}", timeoutTime.toString());
		}
		// Check missing or invalid configuration.
		if (outputFilePath == null || outputFilePath.length() == 0) {
			throw new AlgorithmException("No output file path specified");
		}
		if (sampleSize == null) {
			throw new AlgorithmException("No valid sample size specified");
		}
		File dataset = this.getInput();
		if (dataset == null || !dataset.exists() || !dataset.isFile()) {
			throw new AlgorithmException("No dataset file or an invalid dataset file was given as an input.");
		}
		// Working configuration, so create the actual sample.
		// Write the ARFF header to the output file.
		try {
			outputFileWriter.write(ArffUtilities.extractArffHeader(getInput()));
		} catch (IOException e) {
			throw new AlgorithmException(e, "Error while writing to given output path.");
		}
		// Check if the requested sample size is zero and we can stop directly.
		if (sampleSize == 0) {
			LOG.warn("Sample size is 0, so an empty data set is returned!");
			return new File(outputFilePath);
		}
		// Start the sampling process otherwise.
		this.setState(AlgorithmState.created);
		while (this.hasNext()) {
			try {
				checkAndConductTermination();
			} catch (AlgorithmTimeoutedException e) {
				cleanUp();
				throw new AlgorithmException(e.getMessage());
			}
			if (Instant.now().isAfter(timeoutTime)) {
				LOG.warn("Algorithm is running even though it has been timeouted. Cancelling..");
				this.cancel();
				throw new AlgorithmException("Algorithm is running even though it has been timeouted");
			} else {
				this.next();
			}
		}
		try {
			outputFileWriter.flush();
			outputFileWriter.close();
		} catch (IOException e) {
			cleanUp();
			throw new AlgorithmException(e, "Could not close File writer for sampling output file");
		}
		cleanUp();
		return new File(outputFilePath);
	}

	/**
	 * Implement custom clean up behaviour.
	 */
	protected abstract void cleanUp();

}
