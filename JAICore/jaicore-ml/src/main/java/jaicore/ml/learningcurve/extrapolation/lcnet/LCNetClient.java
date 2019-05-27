package jaicore.ml.learningcurve.extrapolation.lcnet;

/**
 * This class handles the connection to a server that runs pybnn.
 * This way we can use the LCNet from pybnn to get pointwise estimates
 * of learning curves for certain classifiers and configurations of a classifier.
 *
 * @author noni4
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import jaicore.logging.LoggerUtil;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;

public class LCNetClient {

	private Logger logger = LoggerFactory.getLogger(LCNetClient.class);

	// TODO Should not be hardcoded like this
	private static final String SERVER_ADDRESS = "http://localhost:5001/";

	public void train(final int[] xValues, final double[] yValues, final int dataSetSize, final double[][] configurations, final String identifier) throws TrainingException {
		if (xValues.length != yValues.length) {
			throw new IllegalArgumentException("xValues must contain the same number of values as yValues");
		}
		if (xValues.length != configurations.length) {
			throw new IllegalArgumentException("xValues must contain as much numbers as configurations configurations");
		}
		HttpURLConnection httpCon;
		try {
			httpCon = this.establishHttpCon("train", identifier);
		} catch (IOException e1) {
			throw new TrainingException("Could not train", e1);
		}

		JSONObject jsonData = new JSONObject();
		for (int i = 0; i < xValues.length; i++) {
			double[] tmpArray = new double[configurations[i].length + 2];
			for (int j = 0; j < configurations[i].length; j++) {
				tmpArray[j] = configurations[i][j];
			}
			tmpArray[configurations[i].length] = (double) xValues[i] / dataSetSize;
			tmpArray[configurations[i].length + 1] = yValues[i];
			JSONArray allValues = new JSONArray(tmpArray);
			jsonData.put(Integer.toString(i), allValues);
		}

		OutputStreamWriter out;
		try {
			out = new OutputStreamWriter(httpCon.getOutputStream());
			out.write(jsonData.toString());
			out.close();
			httpCon.getInputStream();
		} catch (IOException e) {
			this.logger.error(LoggerUtil.getExceptionInfo(e));
		}
	}

	public double predict(final int xValue, final double[] configurations, final String identifier) throws PredictionException {
		HttpURLConnection httpCon;
		try {
			httpCon = this.establishHttpCon("predict", identifier);

			JSONObject jsonData = new JSONObject();
			double[] tmpArray = new double[configurations.length + 1];
			for (int i = 0; i < configurations.length; i++) {
				tmpArray[i] = configurations[i];
			}
			tmpArray[configurations.length] = xValue;
			JSONArray allValues = new JSONArray(tmpArray);
			jsonData.put("0", allValues);

			OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
			BufferedReader in = null;
			out.write(jsonData.toString());
			out.close();
			in = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));

			StringBuilder inputBuilder = new StringBuilder();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				inputBuilder.append(inputLine);
			}

			HashMap<String, Double> entireInput = null;
			entireInput = new ObjectMapper().readValue(inputBuilder.toString(), HashMap.class);

			return entireInput.get("prediction").doubleValue();
		} catch (IOException e1) {
			throw new PredictionException("Could not predict", e1);
		}
	}

	public void deleteNet(final String identifier) throws IOException {
		HttpURLConnection httpCon = this.establishHttpCon("delete", identifier);

		try (OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());) {
			httpCon.getInputStream();
		}
	}

	private HttpURLConnection establishHttpCon(final String urlParameter, final String identifier) throws IOException {
		URL url = new URL(SERVER_ADDRESS + urlParameter + "/" + identifier);
		HttpURLConnection httpCon = null;
		httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		try {
			httpCon.setRequestMethod("PUT");
		} catch (ProtocolException e) {
			this.logger.error(LoggerUtil.getExceptionInfo(e));
		}
		return httpCon;
	}

}
