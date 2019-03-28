package jaicore.ml.core.dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Utility class for handling Arff dataset files.
 * 
 * @author Lukas Brandt
 */
public class ArffUtilities {

	private ArffUtilities() {
	}

	/**
	 * Extract the header of an ARFF file as a string.
	 * 
	 * @param file Given ARFF file of which the header shall be extracted.
	 * @return Header of the given ARFF file.
	 * @throws IOException Could not read from the given file.
	 */
	public static String extractArffHeader(File file) throws IOException {
		String header = "";
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			if (line.trim().equals("") || line.trim().charAt(0) == '%') {
				continue;
			}
			header += line.trim() + "\n";
			if (line.trim().equals("@data")) {
				break;
			}
		}
		bufferedReader.close();
		return header;
	}

	/**
	 * Counts the amount of datapoint entries in an ARFF file.
	 * 
	 * @param file      Given ARFF file where the entries are written in.
	 * @param hasHeader If true the count will start after an '@data' annotation,
	 *                  otherwise it will just count every line, which is not a
	 *                  comment.
	 * @return Amount of datapoint entries.
	 * @throws IOException Could not read from the given file.
	 */
	public static int countDatasetEntries(File file, boolean hasHeader) throws IOException {
		int result = 0;
		boolean startCounting = !hasHeader;
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			if (line.trim().equals("") || line.trim().charAt(0) == '%') {
				continue;
			}
			if (startCounting) {
				result++;
			} else {
				if (line.trim().equals("@data")) {
					startCounting = true;
				}
			}
		}
		bufferedReader.close();
		return result;
	}

	/**
	 * Skips with a given reader all comment lines and the header lines of an ARFF
	 * file until the first datapoint is reached.
	 * 
	 * @param reader Reader that should be skipped to the data.
	 * @throws IOException Reader was not able to read the file.
	 */
	public static void skipWithReaderToDatapoints(BufferedReader reader) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.trim().equals("") || line.trim().charAt(0) == '%') {
				continue;
			} else if (line.trim().equals("@data")) {
				return;
			}
		}
	}

}
