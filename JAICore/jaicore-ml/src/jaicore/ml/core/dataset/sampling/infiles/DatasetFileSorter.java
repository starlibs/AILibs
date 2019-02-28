package jaicore.ml.core.dataset.sampling.infiles;

import java.io.File;

import jaicore.basic.TempFileHandler;

/**
 * Sorts a Dataset file with a Mergesort. A TempFileHandler can be given or a
 * new one will be created otherwise.
 * 
 * @author Lukas Brandt
 */
public class DatasetFileSorter {
	
	private File datasetFile;
	private TempFileHandler tempFileHandler;
	
	public DatasetFileSorter(File datasetFile, TempFileHandler tempFileHandler) {
		this.datasetFile = datasetFile;
		this.tempFileHandler = tempFileHandler;
	}
	
	public DatasetFileSorter(File datasetFile) {
		this(datasetFile, new TempFileHandler());
	}
	
}
