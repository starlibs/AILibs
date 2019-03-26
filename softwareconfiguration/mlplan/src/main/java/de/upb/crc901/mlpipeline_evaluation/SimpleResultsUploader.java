package de.upb.crc901.mlpipeline_evaluation;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.SupervisedFilterSelector;
import jaicore.basic.SQLAdapter;
import weka.classifiers.Classifier;
import weka.core.OptionHandler;

public class SimpleResultsUploader {

	SQLAdapter adapter;
	String table;
	String algorithmName;
	int experimentId;
	long timeStart = System.currentTimeMillis();

	public SimpleResultsUploader(SQLAdapter adapter, String table, String algorithmName, int experimentId) {
		this.adapter = adapter;
		this.table = table;
		this.algorithmName = algorithmName;
		this.experimentId = experimentId;
	}

	public void uploadResult(MLPipeline classifier, long evaluationTime, double solutionQuality, String phase)
			throws SQLException {
		String solutionString = getSolutionString(classifier);

		if (adapter == null) {
			System.out.println("upload result after " + evaluationTime + " value " + solutionQuality + " solution: "
					+ solutionString);
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
