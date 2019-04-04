package jaicore.ml.skikitwrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class TestProcessListener extends DefaultProcessListener {
	private static final String TEST_RESULTS_PATH_FLAG = "test_results: ";
	private double[] testResults;

	public double[] getTestResults() {
		return testResults;
	}

	@Override
	public void handleError(String error) {
		super.handleError(error);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void handleInput(String input) {
		super.handleInput(input);
		String fileContent = "";
		if (input.startsWith(TEST_RESULTS_PATH_FLAG)) {
			String jsonTestResultsPath = input.substring(TEST_RESULTS_PATH_FLAG.length());
			try {
				fileContent = StringUtils.join(Files.readAllLines(Paths.get(jsonTestResultsPath)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Parse JSON response of python.
			JSONParser parser = new JSONParser();
			ArrayList<Double> tmpResultsList = new ArrayList<>();
			try {
				JSONArray content = (JSONArray) (parser.parse(fileContent));
				JSONArray results = (JSONArray) content.get(0);
				Iterator<Double> iterator = results.iterator();
				while (iterator.hasNext()) {
					tmpResultsList.add(iterator.next());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Transform Double- to double-array.
			testResults = new double[tmpResultsList.size()];
			for (int i = 0; i < tmpResultsList.size(); i++) {
				testResults[i] = tmpResultsList.get(i);
			}
		}
	}
}
