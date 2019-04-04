package jaicore.ml.openml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.Data;
import org.openml.apiconnector.xml.Data.DataSet;
import org.openml.apiconnector.xml.DataFeature;
import org.openml.apiconnector.xml.DataFeature.Feature;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * @author Helena Graf
 *
 */
public class OpenMLHelper {

	private static final Logger logger = LoggerFactory.getLogger(OpenMLHelper.class);

	private static final String DATASET_INDEX = "resources/datasets";

	private static final String API_KEY = "resources/apikey.txt";
	
	private static String apiKey;

	public static List<Integer> getDataSetsFromIndex() throws IOException {
		List<Integer> dataSets = new ArrayList<>();
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(DATASET_INDEX), StandardCharsets.UTF_8)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				int dataSetId = Integer.parseInt(line);
				dataSets.add(dataSetId);
			}
		} catch (IOException e) {
			logger.error("IOException while trying to read data-set indices", e);
		}
		return dataSets;
	}

	/**
	 * Downloads the data set with the given id and returns the Instances file for
	 * it. Will save the {@link org.openml.apiconnector.xml.DataSetDescription} and
	 * the Instances to the location specified in the
	 * {@link org.openml.apiconnector.settings.Settings} Class.
	 * 
	 * @param dataId
	 * @return
	 * @throws IOException
	 */
	public static Instances getInstancesById(int dataId) throws IOException {
		Instances dataset = null;

		if (API_KEY == null) {
			try (BufferedReader reader = Files.newBufferedReader(Paths.get(API_KEY), StandardCharsets.UTF_8)) {
				apiKey = reader.readLine();
			} catch (IOException e) {
				logger.error("Failed to read api_key", e);
			}
		}

		// Get dataset from OpenML
		OpenmlConnector client = new OpenmlConnector();
		try {
			DataSetDescription description = client.dataGet(dataId);
			File file = description.getDataset(OpenMLHelper.apiKey);
			// Instances convert
			DataSource source = new DataSource(file.getCanonicalPath());
			dataset = source.getDataSet();
			dataset.setClassIndex(dataset.numAttributes() - 1);
			Attribute targetAttribute = dataset.attribute(description.getDefault_target_attribute());
			dataset.setClassIndex(targetAttribute.index());
		} catch (Exception e) {
			// These are IOExceptions anyways in the extended sense of this method
			throw new IOException(e.getMessage());
		}
		return dataset;
	}

	/**
	 * Creates a list of data sets by id in a file with caps for the maximum of
	 * features and instances. Caps ignored if set to values <= 0.
	 * 
	 * @param maxNumFeatures
	 * @param maxNumInstances
	 * @throws Exception
	 */
	public static void createDataSetIndex(int maxNumFeatures, int maxNumInstances) throws Exception {
		// For statistics
		int unfiltered;
		int filteredBNG = 0;
		int filteredARFF = 0;
		int filteredTarget = 0;
		int filteredNumeric = 0;
		int fitForAnalysis = 0;

		// For saving data sets
		BufferedWriter writer = Files.newBufferedWriter(
				FileSystems.getDefault().getPath("resources/datasets_" + maxNumFeatures + "_" + maxNumInstances), StandardCharsets.UTF_8);

		// OpenML connection
		OpenmlConnector client = new OpenmlConnector();

		// Get data sets that are active
		HashMap<String, String> map = new HashMap<>();
		map.put("status", "active");
		Data data = client.dataList(map);
		DataSet[] data_raw = data.getData();
		unfiltered = data_raw.length;

		// Filter out data sets not fit for analysis
		for (int i = 0; i < data_raw.length; i++) {
			// Keep track of progress to see if something freezes
			System.out.println("Progress: " + (Math.round(i * 1.0 / data_raw.length * 100.0)));

			// No generated streaming data
			if (data_raw[i].getName().contains("BNG")) {
				filteredBNG++;
				continue;
			}

			// No non-.ARFF files
			if (!data_raw[i].getFormat().equals("ARFF")) {
				filteredARFF++;
				continue;
			}

			// Analyze features
			DataFeature dataFeature = client.dataFeatures(data_raw[i].getDid());
			Feature[] features = dataFeature.getFeatures();
			if (maxNumFeatures > 0 && features.length > maxNumFeatures) {
				continue;
			}

			boolean noTarget = true;
			boolean numericTarget = true;
			for (int j = features.length - 1; j >= 0; j--) {
				if (features[j].getIs_target()) {
					noTarget = false;
					if (features[j].getDataType().equals("numeric")) {
						numericTarget = false;
					}
					break;
				}
			}

			// Analyze instances
			String numInst = data_raw[i].getQualityMap().get("NumberOfInstances");
			if (numInst == null) {
				System.out.println("Couldn't get num inst");
			} else {
				if (Double.parseDouble(numInst) > maxNumInstances) {
					continue;
				}
			}

			// No non-existent target attributes
			if (noTarget) {
				filteredTarget++;
				continue;
			}

			// No numeric target attributes
			if (numericTarget) {
				filteredNumeric++;
				continue;
			}

			// Data is fit for analysis, save
			writer.write(Integer.toString(data_raw[i].getDid()));
			writer.newLine();
			fitForAnalysis++;

		}

		writer.close();

		// Print statistics
		System.out.println("Unfiltered: " + unfiltered);
		System.out.println("BNG: " + filteredBNG);
		System.out.println("ARFF: " + filteredARFF);
		System.out.println("No target: " + filteredTarget);
		System.out.println("Numeric target: " + filteredNumeric);
		System.out.println("Fit for analysis: " + fitForAnalysis);
	}
	
	public static void main (String[] args) {
		try {
			createDataSetIndex(-1, -1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void setApiKey(String apiKey) {
		OpenMLHelper.apiKey = apiKey;
	}

}
