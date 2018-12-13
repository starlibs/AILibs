package de.upb.crc901.mlpipeline_evaluation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WEKAPipelineFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import hasco.model.ComponentInstance;
import hasco.serialization.CompositionSerializer;
import jaicore.basic.SQLAdapter;
import jaicore.ml.openml.OpenMLHelper;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * For caching and evaluation MLPipelines.
 * 
 * @author Helena Graf
 * @author Joshua
 * @author Lukas
 *
 */
public class PipelineEvaluationCache {

	// Evaluation configuration
	private String datasetId;
	private DatasetOrigin datasetOrigin;
	private String testEvaluationTechnique;
	private String testSplitTechnique;
	private int testSeed = 0;
	private String valSplitTechnique;
	private String valEvaluationTechnique;
	private int valSeed = 0;

	private Instances data;
	private SQLAdapter adapter;
	private final String intermediateResultsTableName = "pgotfml_hgraf.intermediate_results";
	private boolean useCache = true;

	/**
	 * Construct a new cache for evaluations. The valid split and evaluation
	 * techniques can be looked up in {@link ConsistentMLPipelineEvaluator}. The
	 * dataset origin can be looked up in {@link DatasetOrigin}.
	 * 
	 * @param datasetId
	 * @param datasetOrigin
	 * @param testEvaluationTechnique
	 * @param testSplitTechnique
	 * @param testSeed
	 * @param valSplitTechnique
	 * @param valEvaluationTechnique
	 * @param valSeed
	 *            Seed for the validation split. Can be null or an empty String
	 * @param adapter
	 *            For the connection to the results table
	 * @throws Exception
	 *             If the dataset cannot be loaded
	 */
	public PipelineEvaluationCache(String datasetId, DatasetOrigin datasetOrigin, String testEvaluationTechnique,
			String testSplitTechnique, int testSeed, String valSplitTechnique, String valEvaluationTechnique,
			int valSeed, SQLAdapter adapter) throws Exception {
		super();
		this.datasetId = datasetId;
		this.datasetOrigin = datasetOrigin;
		this.testEvaluationTechnique = testEvaluationTechnique;
		this.testSplitTechnique = testSplitTechnique;
		this.testSeed = testSeed;
		this.valSplitTechnique = valSplitTechnique;
		this.valEvaluationTechnique = valEvaluationTechnique;
		this.valSeed = valSeed;
		this.adapter = adapter;

		switch (datasetOrigin) {
		case LOCAL:
		case CLUSTER_LOCATION_NEW:
			data = new DataSource(datasetId).getDataSet();
			break;
		case OPENML_DATASET_ID:
			OpenMLHelper.setApiKey("4350e421cdc16404033ef1812ea38c01");
			data = OpenMLHelper.getInstancesById(Integer.parseInt(datasetId));
			break;
		default:
			throw new Exception("Invalid dataset origin.");
		}
	}

	/**
	 * Get an evaluation results for the given pipeline represented by the component
	 * instance in the setting this cache is configured.
	 * 
	 * @param cI
	 * @return Either the looked-up value for the pipeline or the newly evaluated
	 *         result
	 * @throws Exception
	 *             If the pipeline cannot be evaluated
	 */
	public double getResultOrExecuteEvaluation(ComponentInstance cI) throws Exception {
		// Lookup
		String serializedCI = null;
		if (useCache && datasetOrigin != DatasetOrigin.LOCAL) {
			System.out.println("DB Lookup");
			serializedCI = CompositionSerializer.serializeComponentInstance(cI).toString();
			System.out.println("Pipeline: " + serializedCI);
			Double result = doDBLookUp(cI, serializedCI);
			if (result != null) {
				System.out.println("Return DB result");
				return result;
			}
		}

		// Execute
		System.out.println("Execute new evaluation");
		double result = evaluate(cI);
		System.out.println("Score: " + result);

		// Write back
		if (useCache && datasetOrigin != DatasetOrigin.LOCAL) {
			System.out.println("Write new evaluation back into DB");
			uploadResultToDB(cI, serializedCI, result);
		}

		// Return result
		return result;
	}

	private Double doDBLookUp(ComponentInstance cI, String serializedCI) {
		String query;
		List<String> values;
		if (doNotValidate()) {
			query = "SELECT error_rate FROM " + intermediateResultsTableName
					+ " WHERE pipeline=? AND dataset_id=? AND dataset_origin=? AND test_evaluation_technique=? AND test_split_technique=? AND test_seed=? AND val_evaluation_technique IS NULL AND val_split_technique IS NULL AND val_seed IS NULL";
			values = Arrays.asList(serializedCI, datasetId, DatasetOrigin.mapOriginToColumnIdentifier(datasetOrigin),
					testEvaluationTechnique, testSplitTechnique, String.valueOf(testSeed));
		} else {
			query = "SELECT error_rate FROM " + intermediateResultsTableName
					+ " WHERE pipeline=? AND dataset_id=? AND dataset_origin=? AND test_evaluation_technique=? AND test_split_technique=? AND test_seed=? AND val_evaluation_technique=? AND val_split_technique=? AND val_seed=?";
			values = Arrays.asList(serializedCI, datasetId, DatasetOrigin.mapOriginToColumnIdentifier(datasetOrigin),
					testEvaluationTechnique, testSplitTechnique, String.valueOf(testSeed), valEvaluationTechnique,
					valSplitTechnique, String.valueOf(valSeed));
		}

		try {
			ResultSet resultSet = adapter.getResultsOfQuery(query, values);
			if (resultSet.next()) {
				return resultSet.getDouble("error_rate");
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Cannot connect to Cache. Switching to offline mode.");
			this.useCache = false;
			return null;
		}
	}

	private double evaluate(ComponentInstance cI) throws Exception {
		// Get dataset
		MLPipeline classifier = new WEKAPipelineFactory().getComponentInstantiation(cI);
		if (doNotValidate()) {
			return ConsistentMLPipelineEvaluator.evaluateClassifier(testSplitTechnique, testEvaluationTechnique,
					testSeed, data, classifier);
		} else {
			return ConsistentMLPipelineEvaluator.evaluateClassifier(testSplitTechnique, testEvaluationTechnique,
					testSeed, valSplitTechnique, valEvaluationTechnique, valSeed, data, classifier);
		}
	}

	private void uploadResultToDB(ComponentInstance cI, String serializedCI, double result) {
		HashMap<String, Object> map = new HashMap<>();
		map.put("pipeline", serializedCI);
		map.put("dataset_id", datasetId);
		map.put("dataset_origin", DatasetOrigin.mapOriginToColumnIdentifier(datasetOrigin));
		map.put("test_evaluation_technique", testEvaluationTechnique);
		map.put("test_split_technique", testSplitTechnique);
		map.put("test_seed", testSeed);
		map.put("error_rate", result);
		if (!doNotValidate()) {
			map.put("val_split_technique", valSplitTechnique);
			map.put("val_evaluation_technique", valEvaluationTechnique);
			map.put("val_seed", valSeed);
		}

		try {
			adapter.insert(intermediateResultsTableName, map);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Cannot connect to Cache. Switching to offline mode.");
			this.useCache = false;
		}
	}

	private boolean doNotValidate() {
		return valSplitTechnique == null || valSplitTechnique.trim().equals("");
	}

	public void configureValidation(String valSplitTechnique, String valEvaluationTechnique, int valSeed) {
		this.valEvaluationTechnique = valEvaluationTechnique;
		this.valSplitTechnique = valSplitTechnique;
		this.valSeed = valSeed;
	}

	public boolean usesCache() {
		return useCache;
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}
}
