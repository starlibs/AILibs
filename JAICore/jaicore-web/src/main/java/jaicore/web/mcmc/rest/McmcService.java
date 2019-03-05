package jaicore.web.mcmc.rest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.ValidationException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jaicore.web.mcmc.rest.message.ErrorResponse;
import jaicore.web.mcmc.rest.message.LinearCombinationParameterSet;
import jaicore.web.mcmc.rest.message.McmcRequest;
import jaicore.web.mcmc.rest.message.McmcResponse;

@RestController
@RequestMapping("/mcmc")
/**
 * A simple web service which internally calls the tool cmdStan to run MCMC
 * sampling on a precompiled model with the delivered x- and y-coordinates.
 * 
 * @author Felix Weiland
 *
 */
public class McmcService {

	private static Logger LOG = LoggerFactory.getLogger(McmcService.class);

	/**
	 * Name of the file in which the input data are stored in order to be read from
	 * Stan
	 */
	private static final String INPUT_DATA_FILENAME = "input.data.R";

	/** Timeout in ms given to Stan for sampling */
	private static final int STAN_SAMPLING_TIMEOUT_MS = 60000;

	/**
	 * Number of checkpoints at which the sampled function is checked for validity
	 */
	private static final int STAN_SAMPLING_NO_CHECKPOINTS = 10;

	/** Step width between checkpoints */
	private static final int STAN_SAMPLING_CHECKPOINT_INTERVAL = 200;

	/** Number of samples (= curves) to be returned */
	private static final int NUMBER_OF_RETURNED_SAMPLES = 10;

	@PostMapping("/modelparams")
	public ResponseEntity<McmcResponse> computeModelParams(@RequestBody @Valid McmcRequest request) throws Exception {
		validateRequest(request);
		LOG.info("Received valid request: {}", request);
		List<Integer> xValues = request.getxValues();
		List<Double> yValues = request.getyValues();
		Integer numSamples = request.getNumSamples();

		generateInputDataFile(xValues, yValues);

		// Redirect Stan output to a file for debugging purposes
		File outFile = new File("out.log");
		ProcessBuilder pb;
		if (numSamples == null) {
			pb = new ProcessBuilder("stan/lc", "sample", "init=stan/init.R", "data", "file=" + INPUT_DATA_FILENAME);
		} else {
			pb = new ProcessBuilder("stan/lc", "sample", "num_samples=" + numSamples, "init=stan/init.R", "data",
					"file=" + INPUT_DATA_FILENAME);
		}
		pb.redirectError(outFile);
		pb.redirectOutput(outFile);
		LOG.info("Starting Stan. Timeout is {}ms", STAN_SAMPLING_TIMEOUT_MS);
		Process p = pb.start();
		p.waitFor(STAN_SAMPLING_TIMEOUT_MS, TimeUnit.MILLISECONDS);

		LOG.info("Stan finished work. Parsing output..");
		File outputFile = new File("output.csv");

		if (!outFile.exists()) {
			throw new Exception("Stan did not produce output!");
		}

		McmcResponse response = parseOutputFile(outputFile);

		LOG.info("Finished request processing");
		return ResponseEntity.ok().body(response);
	}

	private void validateRequest(McmcRequest request) {
		if (request.getxValues() == null || request.getxValues().isEmpty()) {
			throw new ValidationException("No x values provided!");
		}

		if (request.getyValues() == null || request.getyValues().isEmpty()) {
			throw new ValidationException("No y values provided!");
		}

		if (request.getxValues().size() != request.getyValues().size()) {
			throw new ValidationException("Number of x and y values is not equal!");
		}
	}

	private void generateInputDataFile(List<Integer> xValues, List<Double> yValues) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("N <- ");
		sb.append(xValues.size());
		sb.append("\n");
		sb.append("c <- " + STAN_SAMPLING_NO_CHECKPOINTS + "\n");
		sb.append("s <- " + STAN_SAMPLING_CHECKPOINT_INTERVAL + "\n");
		sb.append("x <- c(");
		for (int i = 0; i < xValues.size(); i++) {
			sb.append(xValues.get(i));
			if (i != xValues.size() - 1) {
				sb.append(",");
			}
		}
		sb.append(")\n");
		sb.append("y <- c(");
		for (int i = 0; i < yValues.size(); i++) {
			sb.append(yValues.get(i));
			if (i != yValues.size() - 1) {
				sb.append(",");
			}
		}
		sb.append(")");
		File inputFile = new File(INPUT_DATA_FILENAME);
		FileUtils.writeStringToFile(inputFile, sb.toString(), Charset.defaultCharset(), false);
	}

	private McmcResponse parseOutputFile(File outputFile) throws IOException {
		// Read raw lines from output file
		List<String> rawLines = FileUtils.readLines(outputFile, Charset.defaultCharset());

		// Include all lines into consideration that contain actual values
		List<String> lines = new ArrayList<>();

		// The first non-comment row is the header and needs to be skipped
		boolean skippedHeader = false;

		// Read line by line
		for (String line : rawLines) {

			// Ignore comments
			if (line.startsWith("#")) {
				continue;
			}

			// Skip first non-comment row
			if (!skippedHeader) {
				skippedHeader = true;
				continue;
			}

			lines.add(line);

		}

		McmcResponse response = new McmcResponse();

		// Choose samples randomly
		while (response.getParameterSets().size() < NUMBER_OF_RETURNED_SAMPLES) {
			int index = ThreadLocalRandom.current().nextInt(0, lines.size());
			String line = lines.get(index);
			response.addParameterSet(parseLine(line));
			lines.remove(index);
		}

		return response;
	}

	private LinearCombinationParameterSet parseLine(String line) {
		double[] values = new double[34];
		String[] parsedLine = line.split(",");
		if (parsedLine.length != values.length) {
			throw new ValidationException(
					String.format("Line has incorrect number of values (%d) : %s", parsedLine.length, line));
		}
		for (int i = 0; i < parsedLine.length; i++) {
			values[i] = Double.parseDouble(parsedLine[i]);
		}

		// Extract parameters
		Map<String, Double> weights = new HashMap<>();
		Map<String, Map<String, Double>> modelParams = new HashMap<>();
		weights.put("log_log_linear", values[7]);
		weights.put("pow_3", values[10]);
		weights.put("log_power", values[14]);
		weights.put("pow_4", values[18]);
		weights.put("mmf", values[23]);
		weights.put("exp_4", values[28]);

		Map<String, Double> logLogLinearParams = new HashMap<>();
		logLogLinearParams.put("a", values[8]);
		logLogLinearParams.put("b", values[9]);
		modelParams.put("log_log_linear", logLogLinearParams);

		Map<String, Double> pow3Params = new HashMap<>();
		pow3Params.put("c", values[11]);
		pow3Params.put("a", values[12]);
		pow3Params.put("alpha", values[13]);
		modelParams.put("pow_3", pow3Params);

		Map<String, Double> logPowerParams = new HashMap<>();
		logPowerParams.put("a", values[15]);
		logPowerParams.put("b", values[16]);
		logPowerParams.put("c", values[17]);
		modelParams.put("log_power", logPowerParams);

		Map<String, Double> pow4Params = new HashMap<>();
		pow4Params.put("a", values[19]);
		pow4Params.put("b", values[20]);
		pow4Params.put("c", values[21]);
		pow4Params.put("alpha", values[22]);
		modelParams.put("pow_4", pow4Params);

		Map<String, Double> mmfParams = new HashMap<>();
		mmfParams.put("alpha", values[24]);
		mmfParams.put("beta", values[25]);
		mmfParams.put("delta", values[26]);
		mmfParams.put("kappa", values[27]);
		modelParams.put("mmf", mmfParams);

		Map<String, Double> exp4Params = new HashMap<>();
		exp4Params.put("a", values[29]);
		exp4Params.put("b", values[30]);
		exp4Params.put("c", values[31]);
		exp4Params.put("alpha", values[32]);
		modelParams.put("exp_4", exp4Params);

		return new LinearCombinationParameterSet(weights, modelParams);
	}

	@ExceptionHandler({ Exception.class })
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		LOG.error("Exception in request processing. Returning error message!", e);
		ErrorResponse er = new ErrorResponse(e.getLocalizedMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(er);
	}

}