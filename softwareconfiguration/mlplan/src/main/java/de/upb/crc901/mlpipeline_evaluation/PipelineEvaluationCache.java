package de.upb.crc901.mlpipeline_evaluation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private Logger logger = LoggerFactory.getLogger(PipelineEvaluationCache.class);
	private static final String LOG_CANT_CONNECT_TO_CACHE = "Cannot connect to cache. Switching to offline mode.";

	private static final String INTERMEDIATE_RESULTS_TABLENAME = "pgotfml_hgraf.intermediate_results";

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
	private boolean useCache = true;

	/**
	 * Construct a new cache for evaluations. The valid split and evaluation
	 * techniques can be looked up in {@link ConsistentMLPipelineEvaluator}. The
	 * dataset origin can be looked up in {@link DatasetOrigin}.
	 *
	 * @param configBuilder
	 * @throws NumberFormatException
	 * @throws Exception
	 *             If the dataset cannot be loaded
	 */
	public PipelineEvaluationCache(final PipelineEvaluationCacheConfigBuilder configBuilder) throws Exception {
		super();
		this.datasetId = configBuilder.getDatasetId();
		this.datasetOrigin = configBuilder.getDatasetOrigin();
		this.testEvaluationTechnique = configBuilder.getTestEvaluationTechnique();
		this.testSplitTechnique = configBuilder.getTestSplitTechnique();
		this.testSeed = configBuilder.getTestSeed();
		this.valEvaluationTechnique = configBuilder.getValEvaluationTechnique();
		this.valSplitTechnique = configBuilder.getValSplitTechnique();
		this.valSeed = configBuilder.getValSeed();
		this.adapter = configBuilder.getAdapter();

		switch (this.datasetOrigin) {
		case LOCAL:
		case CLUSTER_LOCATION_NEW:
			this.data = new DataSource(this.datasetId).getDataSet();
			break;
		case OPENML_DATASET_ID:
			OpenMLHelper.setApiKey("4350e421cdc16404033ef1812ea38c01");
			this.data = OpenMLHelper.getInstancesById(Integer.parseInt(this.datasetId));
			break;
		default:
			throw new InvalidDatasetOriginException("Invalid dataset origin.");
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
	public double getResultOrExecuteEvaluation(final ComponentInstance cI) throws Exception {
		// Lookup
		String serializedCI = null;
		if (this.useCache && this.datasetOrigin != DatasetOrigin.LOCAL) {
			this.logger.debug("DB Lookup");
			serializedCI = CompositionSerializer.serializeComponentInstance(cI).toString();
			this.logger.debug("Pipeline: {}", serializedCI);
			Double result = this.doDBLookUp(serializedCI);
			if (result != null) {
				this.logger.debug("Return DB result");
				return result;
			}
		}

		// Execute
		this.logger.debug("Execute new evaluation");
		double result = this.evaluate(cI);
		this.logger.debug("Score: {}", result);

		// Write back
		if (this.useCache && this.datasetOrigin != DatasetOrigin.LOCAL) {
			this.logger.debug("Write new evaluation back into DB");
			this.uploadResultToDB(serializedCI, result);
		}

		// Return result
		return result;
	}

	private Double doDBLookUp(final String serializedCI) {
		String query;
		List<String> values;
		if (this.doNotValidate()) {
			query = "SELECT error_rate FROM " + INTERMEDIATE_RESULTS_TABLENAME
					+ " WHERE pipeline=? AND dataset_id=? AND dataset_origin=? AND test_evaluation_technique=? AND test_split_technique=? AND test_seed=? AND val_evaluation_technique IS NULL AND val_split_technique IS NULL AND val_seed IS NULL";
			values = Arrays.asList(serializedCI, this.datasetId, DatasetOrigin.mapOriginToColumnIdentifier(this.datasetOrigin), this.testEvaluationTechnique, this.testSplitTechnique, String.valueOf(this.testSeed));
		} else {
			query = "SELECT error_rate FROM " + INTERMEDIATE_RESULTS_TABLENAME
					+ " WHERE pipeline=? AND dataset_id=? AND dataset_origin=? AND test_evaluation_technique=? AND test_split_technique=? AND test_seed=? AND val_evaluation_technique=? AND val_split_technique=? AND val_seed=?";
			values = Arrays.asList(serializedCI, this.datasetId, DatasetOrigin.mapOriginToColumnIdentifier(this.datasetOrigin), this.testEvaluationTechnique, this.testSplitTechnique, String.valueOf(this.testSeed),
					this.valEvaluationTechnique, this.valSplitTechnique, String.valueOf(this.valSeed));
		}

		try {
			ResultSet resultSet = this.adapter.getResultsOfQuery(query, values);
			if (resultSet.next()) {
				return resultSet.getDouble("error_rate");
			} else {
				return null;
			}
		} catch (SQLException e) {
			this.logger.warn(LOG_CANT_CONNECT_TO_CACHE, e);
			this.useCache = false;
			return null;
		}
	}

	private double evaluate(final ComponentInstance cI) throws Exception {
		// Get dataset
		MLPipeline classifier = new WEKAPipelineFactory().getComponentInstantiation(cI);
		if (this.doNotValidate()) {
			return ConsistentMLPipelineEvaluator.evaluateClassifier(this.testSplitTechnique, this.testEvaluationTechnique, this.testSeed, this.data, classifier);
		} else {
			return ConsistentMLPipelineEvaluator.evaluateClassifier(this.testSplitTechnique, this.testEvaluationTechnique, this.testSeed, this.valSplitTechnique, this.valEvaluationTechnique, this.valSeed, this.data, classifier);
		}
	}

	private void uploadResultToDB(final String serializedCI, final double result) {
		HashMap<String, Object> map = new HashMap<>();
		map.put("pipeline", serializedCI);
		map.put("dataset_id", this.datasetId);
		map.put("dataset_origin", DatasetOrigin.mapOriginToColumnIdentifier(this.datasetOrigin));
		map.put("test_evaluation_technique", this.testEvaluationTechnique);
		map.put("test_split_technique", this.testSplitTechnique);
		map.put("test_seed", this.testSeed);
		map.put("error_rate", result);
		if (!this.doNotValidate()) {
			map.put("val_split_technique", this.valSplitTechnique);
			map.put("val_evaluation_technique", this.valEvaluationTechnique);
			map.put("val_seed", this.valSeed);
		}

		try {
			this.adapter.insert(INTERMEDIATE_RESULTS_TABLENAME, map);
		} catch (SQLException e) {
			this.logger.warn(LOG_CANT_CONNECT_TO_CACHE, e);
			this.useCache = false;
		}
	}

	private boolean doNotValidate() {
		return this.valSplitTechnique == null || this.valSplitTechnique.trim().equals("");
	}

	public void configureValidation(final String valSplitTechnique, final String valEvaluationTechnique, final int valSeed) {
		this.valEvaluationTechnique = valEvaluationTechnique;
		this.valSplitTechnique = valSplitTechnique;
		this.valSeed = valSeed;
	}

	public boolean usesCache() {
		return this.useCache;
	}

	public void setUseCache(final boolean useCache) {
		this.useCache = useCache;
	}
}
