package de.upb.crc901.mlpipeline_evaluation;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.SupervisedFilterSelector;
import jaicore.basic.SQLAdapter;
import weka.classifiers.Classifier;
import weka.core.OptionHandler;

/**
 * Uploads intermediate evaluations during a run of ML-Plan.
 * 
 * @author Helena Graf
 *
 */
public class SimpleResultsUploader {

	private Logger logger = LoggerFactory.getLogger(SimpleResultsUploader.class);

	/**
	 * db adapter for uploading
	 */
	private SQLAdapter adapter;

	/**
	 * table to use for intermediate results
	 */
	private String table;

	/**
	 * the name of the algorithm for which results are uploaded
	 */
	private String algorithmName;

	/**
	 * the id of the experiment for which results are uploaded
	 */
	private int experimentId;

	/**
	 * start time of the search
	 */
	private long timeStart = System.currentTimeMillis();

	/**
	 * Construct a new simple results uploader with the given configuration for the
	 * entries and table.
	 * 
	 * @param adapter
	 *            db adapter for uploading
	 * @param table
	 *            table to use for intermediate results
	 * @param algorithmName
	 *            the name of the algorithm for which results are uploaded
	 * @param experimentId
	 *            the id of the experiment for which results are uploaded
	 */
	public SimpleResultsUploader(SQLAdapter adapter, String table, String algorithmName, int experimentId) {
		this.adapter = adapter;
		this.table = table;
		this.algorithmName = algorithmName;
		this.experimentId = experimentId;
	}

	/**
	 * Uploads the given intermediate results
	 * 
	 * @param classifier
	 *            the pipeline for which a result was found
	 * @param evaluationTime
	 *            the time it took to evaluate the pipeline
	 * @param solutionQuality
	 *            the error of the pipeline
	 * @param phase
	 *            the phase in which the pipeline was found (search or selection)
	 * @throws SQLException
	 */
	public void uploadResult(MLPipeline classifier, long evaluationTime, double solutionQuality, String phase)
			throws SQLException {
		String solutionString = getSolutionString(classifier);

		if (adapter == null) {
			logger.warn("Not uploading result after {}ms with value {}. Solution: {}", evaluationTime, solutionQuality,
					solutionString);
		} else {
			Map<String, Object> map = new HashMap<>();
			map.put("classifier", solutionString);
			map.put("phase", phase);
			map.put("loss", solutionQuality);
			map.put("time", evaluationTime);
			map.put("found", System.currentTimeMillis() - timeStart);
			map.put("algorithm", algorithmName);
			map.put("experiment_id", experimentId);
			adapter.insert(table, map);
		}
	}

	/**
	 * Converts the given pipeline to a simple string representation
	 * 
	 * @param classifier
	 *            the pipeline to convert
	 * @return the string representation
	 */
	public static String getSolutionString(MLPipeline classifier) {
		if (classifier == null) {
			return "error";
		}

		Classifier baseClassifier = classifier.getBaseClassifier();
		String[] classifierOptionsArray;
		String classifierOptionsString = "";
		String classifierString = baseClassifier.getClass().getName();
		if (baseClassifier instanceof OptionHandler) {
			classifierOptionsArray = ((OptionHandler) baseClassifier).getOptions();
			classifierOptionsString = classifierOptionsArray.length > 0
					? Arrays.stream(classifierOptionsArray).collect(Collectors.joining(", ", "[", "]"))
					: "";
		}

		SupervisedFilterSelector preprocessor = !classifier.getPreprocessors().isEmpty()
				? classifier.getPreprocessors().get(0)
				: null;
		String preprocessorString = preprocessor != null ? preprocessor.getClass().getName() : "";

		String[] preprocessorOptionsArray;
		String preprocessorOptionsString = "";
		if (preprocessor instanceof OptionHandler) {
			preprocessorOptionsArray = !preprocessorString.equals("") ? ((OptionHandler) preprocessor).getOptions()
					: new String[0];
			preprocessorOptionsString = preprocessorOptionsArray.length > 0
					? Arrays.stream(preprocessorOptionsArray).collect(Collectors.joining(",", "[", "]"))
					: "";
		}

		return classifierString + " " + classifierOptionsString + " " + preprocessorString + " "
				+ preprocessorOptionsString;
	}
}
