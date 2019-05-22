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
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LCNetClient {

	private Logger logger = LoggerFactory.getLogger(LCNetClient.class);

	// TODO Should not be hardcoded like this
	private static final String SERVER_ADDRESS = "http://localhost:5001/";

	public void train(int[] xValues, double[] yValues, int dataSetSize, double[][] configurations, String identifier) {
		if (xValues.length != yValues.length)
			throw new RuntimeException("xValues must contain the same number of values as yValues");
		if (xValues.length != configurations.length)
			throw new RuntimeException("xValues must contain as much numbers as configurations configurations");
		HttpURLConnection httpCon = this.establishHttpCon("train", identifier);

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
			logger.error("Unexpected exception", e);
		}
	}

	public double predict(int xValue, double[] configurations, String identifier) {
		HttpURLConnection httpCon = this.establishHttpCon("predict", identifier);

		JSONObject jsonData = new JSONObject();
		double[] tmpArray = new double[configurations.length + 1];
		for (int i = 0; i < configurations.length; i++) {
			tmpArray[i] = configurations[i];
		}
		tmpArray[configurations.length] = xValue;
		JSONArray allValues = new JSONArray(tmpArray);
		jsonData.put("0", allValues);

		OutputStreamWriter out;
		BufferedReader in = null;
		try {
			out = new OutputStreamWriter(httpCon.getOutputStream());
			out.write(jsonData.toString());
			out.close();
			in = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
		} catch (IOException e) {
			throw new RuntimeException("Cannot read JSON data", e);
		}

		StringBuilder inputBuilder = new StringBuilder();
		String inputLine;
		try {
			while ((inputLine = in.readLine()) != null) {
				inputBuilder.append(inputLine);
			}
		} catch (IOException e) {
			throw new RuntimeException("Cannot read data", e);
		}

		HashMap<String, Double> entireInput = null;
		try {
			entireInput = new ObjectMapper().readValue(inputBuilder.toString(), HashMap.class);
		} catch (IOException e) {
			throw new RuntimeException("Unexpected read data", e);
		}

		return entireInput.get("prediction").doubleValue();
	}

	public void deleteNet(String identifier) {
		HttpURLConnection httpCon = this.establishHttpCon("delete", identifier);

		OutputStreamWriter out;
		try {
			out = new OutputStreamWriter(httpCon.getOutputStream());
			out.close();
			httpCon.getInputStream();
		} catch (IOException e) {
			throw new RuntimeException("Unexpected exception", e);
		}
	}

	private HttpURLConnection establishHttpCon(String urlParameter, String identifier) {
		URL url = null;
		try {
			url = new URL(SERVER_ADDRESS + urlParameter + "/" + identifier);
		} catch (MalformedURLException e) {
			throw new RuntimeException("URL is malformed", e);
		}
		HttpURLConnection httpCon = null;
		try {
			httpCon = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			throw new RuntimeException("Cannot open connection", e);
		}
		httpCon.setDoOutput(true);
		try {
			httpCon.setRequestMethod("PUT");
		} catch (ProtocolException e) {
			logger.error("Unexpected exception", e);
		}
		return httpCon;
	}

}
