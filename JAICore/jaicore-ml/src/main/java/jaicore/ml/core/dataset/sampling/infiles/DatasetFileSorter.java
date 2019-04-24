package jaicore.ml.core.dataset.sampling.infiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;

import jaicore.basic.TempFileHandler;
import jaicore.ml.core.dataset.ArffUtilities;

/**
 * Sorts a Dataset file with a Mergesort. A TempFileHandler can be given or a
 * new one will be created otherwise.
 * 
 * @author Lukas Brandt
 */
public class DatasetFileSorter {

	private File datasetFile;
	private TempFileHandler tempFileHandler;
	private boolean usesOwnTempFileHandler;

	// Default comperator, which compared the single features as strings
	private Comparator<String> comparator = (s1, s2) -> {
		String[] f1 = s1.trim().split(",");
		String[] f2 = s2.trim().split(",");
		int l = Math.min(f1.length, f2.length);
		for (int i = 0; i < l; i++) {
			int c = f1[i].compareTo(f2[i]);
			if (c != 0) {
				return c;
			}
		}
		return 0;
	};

	public DatasetFileSorter(File datasetFile, TempFileHandler tempFileHandler) {
		this.datasetFile = datasetFile;
		this.tempFileHandler = tempFileHandler;
		this.usesOwnTempFileHandler = false;
	}

	public DatasetFileSorter(File datasetFile) {
		this(datasetFile, new TempFileHandler());
		this.usesOwnTempFileHandler = true;
	}

	/**
	 * @param comparator
	 *            Custom comparator for the dataset file lines.
	 */
	public void setComparator(Comparator<String> comparator) {
		this.comparator = comparator;
	}

	/**
	 * 
	 * @param sortedFilePath
	 * @return
	 * @throws IOException
	 */
	public File sort(String sortedFilePath) throws IOException {
		IOException exception;
		try (FileWriter fileWriter = new FileWriter(new File(sortedFilePath));
				FileReader fr = new FileReader(this.datasetFile);
				BufferedReader datasetFileReader = new BufferedReader(fr)) {
			// Create a new file for the sorted dataset with the ARFF header
			String arffHeader;
			arffHeader = ArffUtilities.extractArffHeader(this.datasetFile);
			fileWriter.write(arffHeader);

			// Create a temp file with all datapoints
			String tempFileUUID = this.tempFileHandler.createTempFile();
			FileWriter tempFileWriter = this.tempFileHandler.getFileWriterForTempFile(tempFileUUID);
			String dataPointLine;
			boolean datastarted = false;
			while ((dataPointLine = datasetFileReader.readLine()) != null) {
				if (dataPointLine.trim().equals("") || dataPointLine.trim().charAt(0) == '%') {
					continue;
				}
				if (datastarted) {
					tempFileWriter.write(dataPointLine.trim() + "\n");
				} else {
					if (dataPointLine.trim().equals("@data")) {
						datastarted = true;
					}
				}
			}
			tempFileWriter.flush();
			datasetFileReader.close();

			// Sort the temp file
			String sortedFileUUID = mergesort(tempFileUUID);

			// Write the sorted lines from the temp file to the output file.
			BufferedReader sortedReader = this.tempFileHandler.getFileReaderForTempFile(sortedFileUUID);
			String line;
			while ((line = sortedReader.readLine()) != null) {
				fileWriter.write(line + "\n");
			}
			fileWriter.flush();
			return new File(sortedFilePath);
		} catch (IOException e) {
			exception = e;
		} finally {
			// Start clean up of the temporary file handler if a new one was used for this
			// sorting.
			if (usesOwnTempFileHandler) {
				this.tempFileHandler.close();
			}
		}
		throw exception;
	}

	private String mergesort(String fileUUID) throws IOException {
		int length = ArffUtilities.countDatasetEntries(this.tempFileHandler.getTempFile(fileUUID), false);
		if (length <= 1) {
			return fileUUID;
		} else {
			// Split the existing file into two halfs
			String leftUUID = this.tempFileHandler.createTempFile();
			String rightUUID = this.tempFileHandler.createTempFile();
			FileWriter leftWriter = this.tempFileHandler.getFileWriterForTempFile(leftUUID);
			FileWriter rightWriter = this.tempFileHandler.getFileWriterForTempFile(rightUUID);
			BufferedReader reader = this.tempFileHandler.getFileReaderForTempFile(fileUUID);
			int i = 0;
			String line;
			while ((line = reader.readLine()) != null) {
				if (i < (length / 2)) {
					leftWriter.write(line + "\n");
				} else {
					rightWriter.write(line + "\n");
				}
				i++;
			}
			leftWriter.flush();
			rightWriter.flush();
			// Sort the two halfs
			String sortedLeftUUID = mergesort(leftUUID);
			String sortedRightUUID = mergesort(rightUUID);
			// Merge the sorted halfs back together ande delete the left and right temp
			// files
			String mergedFileUUID = merge(sortedLeftUUID, sortedRightUUID);
			this.tempFileHandler.deleteTempFile(leftUUID);
			this.tempFileHandler.deleteTempFile(rightUUID);
			return mergedFileUUID;
		}
	}

	private String merge(String leftUUID, String rightUUID) throws IOException {
		String uuid = this.tempFileHandler.createTempFile();
		FileWriter writer = this.tempFileHandler.getFileWriterForTempFile(uuid);
		BufferedReader leftReader = this.tempFileHandler.getFileReaderForTempFile(leftUUID);
		BufferedReader rightReader = this.tempFileHandler.getFileReaderForTempFile(rightUUID);
		String leftLine = leftReader.readLine();
		String rightLine = rightReader.readLine();
		while (leftLine != null || rightLine != null) {
			if (leftLine == null) {
				writer.write(rightLine + "\n");
				rightLine = rightReader.readLine();
			} else if (rightLine == null) {
				writer.write(leftLine + "\n");
				leftLine = leftReader.readLine();
			} else {
				int c = this.comparator.compare(leftLine, rightLine);
				if (c > 0) {
					writer.write(rightLine + "\n");
					rightLine = rightReader.readLine();
				} else {
					writer.write(leftLine + "\n");
					leftLine = leftReader.readLine();
				}
			}
		}
		writer.flush();
		return uuid;
	}

}
