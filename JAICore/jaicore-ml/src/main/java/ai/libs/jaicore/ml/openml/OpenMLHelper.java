package ai.libs.jaicore.ml.openml;

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

	private OpenMLHelper() {
		/* avoid instantiation */
	}

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

	public static DataSource getDataSourceById(final int dataId) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(API_KEY), StandardCharsets.UTF_8)) {
			apiKey = reader.readLine();
		} catch (IOException e) {
			logger.error("Failed to read api_key", e);
		}

		// Get dataset from OpenML
		OpenmlConnector client = new OpenmlConnector();
		try {
			DataSetDescription description = client.dataGet(dataId);
			File file = description.getDataset(OpenMLHelper.apiKey);
			// Instances convert
			return new DataSource(file.getCanonicalPath());
		} catch (Exception e) {
			// These are IOExceptions anyways in the extended sense of this method
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Downloads the data set with the given id and returns the Instances file for it. Will save the {@link org.openml.apiconnector.xml.DataSetDescription} and the Instances to the location specified in the
	 * {@link org.openml.apiconnector.settings.Settings} Class.
	 *
	 * @param dataId
	 * @return
	 * @throws IOException
	 *             if something goes wrong while loading Instances from openml
	 */
	public static Instances getInstancesById(final int dataId) throws IOException {
		Instances dataset = null;
		try {
			OpenmlConnector client = new OpenmlConnector();
			DataSetDescription description = client.dataGet(dataId);
			DataSource source = getDataSourceById(dataId);
			dataset = source.getDataSet();
			Attribute targetAttribute = dataset.attribute(description.getDefault_target_attribute());
			dataset.setClassIndex(targetAttribute.index());
		} catch (Exception e) {
			// These are IOExceptions anyways in the extended sense of this method
			throw new IOException(e.getMessage());
		}
		return dataset;
	}

	/**
	 * Creates a list of data sets by id in a file with caps for the maximum of features and instances. Caps ignored if set to values <= 0.
	 *
	 * @param maxNumFeatures
	 * @param maxNumInstances
	 * @throws Exception
	 */
	public static void createDataSetIndex(final int maxNumFeatures, final int maxNumInstances) throws Exception {
		// For statistics
		int unfiltered;
		int filteredBNG = 0;
		int filteredARFF = 0;
		int filteredTarget = 0;
		int filteredNumeric = 0;
		int fitForAnalysis = 0;

		// For saving data sets
		try (BufferedWriter writer = Files.newBufferedWriter(FileSystems.getDefault().getPath("resources/datasets_" + maxNumFeatures + "_" + maxNumInstances), StandardCharsets.UTF_8)) {

			// OpenML connection
			OpenmlConnector client = new OpenmlConnector();

			// Get data sets that are active
			HashMap<String, String> map = new HashMap<>();
			map.put("status", "active");
			Data data = client.dataList(map);
			DataSet[] dataRaw = data.getData();
			unfiltered = dataRaw.length;

			// Filter out data sets not fit for analysis
			for (int i = 0; i < dataRaw.length; i++) {
				// Keep track of progress to see if something freezes
				logger.info("Progress: {}", (Math.round(i * 1.0 / dataRaw.length * 100.0)));

				// No generated streaming data
				if (dataRaw[i].getName().contains("BNG")) {
					filteredBNG++;
					continue;
				}

				// No non-.ARFF files
				if (!dataRaw[i].getFormat().equals("ARFF")) {
					filteredARFF++;
					continue;
				}

				// Analyze features
				DataFeature dataFeature = client.dataFeatures(dataRaw[i].getDid());
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
				String numInst = dataRaw[i].getQualityMap().get("NumberOfInstances");
				if (numInst == null) {
					logger.info("Couldn't get num inst");
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
				writer.write(Integer.toString(dataRaw[i].getDid()));
				writer.newLine();
				fitForAnalysis++;

			}

			// Print statistics
			logger.info("Unfiltered: {}", unfiltered);
			logger.info("BNG: {}", filteredBNG);
			logger.info("ARFF: {}", filteredARFF);
			logger.info("No target: {}", filteredTarget);
			logger.info("Numeric target: {}", filteredNumeric);
			logger.info("Fit for analysis: {}", fitForAnalysis);
		}
	}

	public static void setApiKey(final String apiKey) {
		OpenMLHelper.apiKey = apiKey;
	}

}
