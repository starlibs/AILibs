package jaicore.ml.core.dataset.sampling.infiles.stratified.sampling;

import java.util.Map;

import jaicore.basic.TempFileHandler;
import jaicore.basic.algorithm.exceptions.AlgorithmException;

/**
 * Interface to implement custom Stratum assignment behavior. A temporary file
 * for each Stratum has to be created and the corresponding datapoints written
 * into it. Each temporary file has to be a valid subset of the input file, i.e
 * the ARFF header has to be written at the top of the file.
 * 
 * @author Lukas Brandt
 */
public interface IStratiFileAssigner {

	/**
	 * Set the temporary file handler, which will be used to manage the temporary
	 * files for the strati.
	 * 
	 * @param tempFileHandler Temporary File Handler to manage the files.
	 */
	public void setTempFileHandler(TempFileHandler tempFileHandler);

	/**
	 * Set the header of the original ARFF input file. It has to be written on top
	 * of each temporary file. Besides of that it can be used to extract meta data
	 * about the dataset if needed.
	 * 
	 * @param arffHeader ARFF header lines as a string.
	 */
	public void setArffHeader(String arffHeader);

	/**
	 * Select the suitable stratum for a datapoint and write it into the
	 * corresponding temporary file.
	 * 
	 * @param datapoint String representation of the datapoint taken from the input
	 *                  file.
	 * @throws AlgorithmException The datapoint representation was invalid or it
	 *                            could not be assigned to a stratum.
	 */
	public void assignDatapoint(String datapoint) throws AlgorithmException;

	/**
	 * Get the used strati temporary files and the amount of datapoints inside of
	 * it.
	 * 
	 * @return Mapping from UUID of the temporary file of a strati to the number of
	 *         datapoints written into it.
	 */
	public Map<String, Integer> getAllCreatedStrati();

}
