package de.upb.crc901.mlpipeline_evaluation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WEKAPipelineFactory;
import hasco.model.ComponentInstance;
import hasco.serialization.CompositionSerializer;
import jaicore.ml.openml.OpenMLHelper;
import weka.classifiers.Classifier;
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
	private boolean useCache = true;

	private final PipelineEvaluationCacheConfigBuilder config;

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
		this.config = configBuilder;

		switch (this.config.getDatasetOrigin()) {
		case LOCAL:
		case CLUSTER_LOCATION_NEW:
			this.config.withDataset(new DataSource(this.config.getDatasetId()).getDataSet());
			break;
		case OPENML_DATASET_ID:
			OpenMLHelper.setApiKey("4350e421cdc16404033ef1812ea38c01");
			this.config.withDataset(OpenMLHelper.getInstancesById(Integer.parseInt(this.config.getDatasetId())));
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
		if (this.useCache && this.config.getDatasetOrigin() != DatasetOrigin.LOCAL) {
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
		if (this.useCache && this.config.getDatasetOrigin() != DatasetOrigin.LOCAL) {
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
			values = Arrays.asList(serializedCI, this.config.getDatasetId(), DatasetOrigin.mapOriginToColumnIdentifier(this.config.getDatasetOrigin()), this.config.getTestEvaluationTechnique(), this.config.getTestSplitTechnique(),
					String.valueOf(this.config.getTestSeed()));
		} else {
			query = "SELECT error_rate FROM " + INTERMEDIATE_RESULTS_TABLENAME
					+ " WHERE pipeline=? AND dataset_id=? AND dataset_origin=? AND test_evaluation_technique=? AND test_split_technique=? AND test_seed=? AND val_evaluation_technique=? AND val_split_technique=? AND val_seed=?";
			values = Arrays.asList(serializedCI, this.config.getDatasetId(), DatasetOrigin.mapOriginToColumnIdentifier(this.config.getDatasetOrigin()), this.config.getTestEvaluationTechnique(), this.config.getTestSplitTechnique(),
					String.valueOf(this.config.getTestSeed()), this.config.getValEvaluationTechnique(), this.config.getValSplitTechnique(), String.valueOf(this.config.getValSeed()));
		}

		try {
			ResultSet resultSet = this.config.getAdapter().getResultsOfQuery(query, values);
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
		Classifier classifier = new WEKAPipelineFactory().getComponentInstantiation(cI);
		if (this.doNotValidate()) {
			return ConsistentMLPipelineEvaluator.evaluateClassifier(this.config.getTestSplitTechnique(), this.config.getTestEvaluationTechnique(), this.config.getTestSeed(), this.config.getData(), classifier);
		} else {
			return ConsistentMLPipelineEvaluator.evaluateClassifier(this.config.getTestSplitTechnique(), this.config.getTestEvaluationTechnique(), this.config.getTestSeed(), this.config.getValSplitTechnique(),
					this.config.getValEvaluationTechnique(), this.config.getValSeed(), this.config.getData(), classifier);
		}
	}

	private void uploadResultToDB(final String serializedCI, final double result) {
		HashMap<String, Object> map = new HashMap<>();
		map.put("pipeline", serializedCI);
		map.put("dataset_id", this.config.getDatasetId());
		map.put("dataset_origin", DatasetOrigin.mapOriginToColumnIdentifier(this.config.getDatasetOrigin()));
		map.put("test_evaluation_technique", this.config.getTestEvaluationTechnique());
		map.put("test_split_technique", this.config.getTestSplitTechnique());
		map.put("test_seed", this.config.getTestSeed());
		map.put("error_rate", result);
		if (!this.doNotValidate()) {
			map.put("val_split_technique", this.config.getValSplitTechnique());
			map.put("val_evaluation_technique", this.config.getValEvaluationTechnique());
			map.put("val_seed", this.config.getValSeed());
		}

		try {
			this.config.getAdapter().insert(INTERMEDIATE_RESULTS_TABLENAME, map);
		} catch (SQLException e) {
			this.logger.warn(LOG_CANT_CONNECT_TO_CACHE, e);
			this.useCache = false;
		}
	}

	private boolean doNotValidate() {
		return this.config.getValSplitTechnique() == null || this.config.getValSplitTechnique().trim().equals("");
	}

	public void configureValidation(final String valSplitTechnique, final String valEvaluationTechnique, final int valSeed) {
		this.config.withValEvaluationTechnique(valEvaluationTechnique);
		this.config.withValSplitTechnique(valSplitTechnique);
		this.config.withValSeed(valSeed);
	}

	public boolean usesCache() {
		return this.useCache;
	}

	public void setUseCache(final boolean useCache) {
		this.useCache = useCache;
	}
}
