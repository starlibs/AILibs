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
	 * @param file Given ARFF file of which the header shall be extracted.
	 * @return Header of the given ARFF file.
	 * @throws IOException Could not read from the given file.
	 */
	public static String extractArffHeader(File file) throws IOException {
		String header = "";
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			header += line.trim() + "\n";
			if (line.trim().equals("@data")) {
				break;
			}
		}
		bufferedReader.close();
		return header;
	}
	
	public static int countDatasetEntries(File file) throws IOException {
		int result = 0;
		boolean startCounting = false;
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = bufferedReader.readLine()) != null) {
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

}
