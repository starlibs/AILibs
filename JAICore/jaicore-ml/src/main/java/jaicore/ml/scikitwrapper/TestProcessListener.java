package jaicore.ml.scikitwrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestProcessListener extends DefaultProcessListener {

	private static final String TEST_RESULTS_PATH_FLAG = "test_results: ";
	private List<List<Double>> testResults;

	public TestProcessListener(boolean verbose) {
		super(verbose);
	}

	public List<List<Double>> getTestResults() {
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
				// Parse JSON response of python.
				ObjectMapper objMapper = new ObjectMapper();
				testResults = (List<List<Double>>) objMapper.readValue(fileContent, List.class).get(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
