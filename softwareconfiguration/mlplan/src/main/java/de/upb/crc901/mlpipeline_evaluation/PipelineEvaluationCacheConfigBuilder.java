package de.upb.crc901.mlpipeline_evaluation;

import jaicore.basic.SQLAdapter;
import weka.core.Instances;

public class PipelineEvaluationCacheConfigBuilder {

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

	public PipelineEvaluationCacheConfigBuilder() {
		super();
	}

	public PipelineEvaluationCacheConfigBuilder withDatasetID(final String datasetId) {
		this.datasetId = datasetId;
		return this;
	}

	public PipelineEvaluationCacheConfigBuilder withDatasetOrigin(final DatasetOrigin datasetOrigin) {
		this.datasetOrigin = datasetOrigin;
		return this;
	}

	public PipelineEvaluationCacheConfigBuilder withTestEvaluationTechnique(final String testEvaluationTechnique) {
		this.testEvaluationTechnique = testEvaluationTechnique;
		return this;
	}

	public PipelineEvaluationCacheConfigBuilder withtestSplitTechnique(final String testSplitTechnique) {
		this.testSplitTechnique = testSplitTechnique;
		return this;
	}

	public PipelineEvaluationCacheConfigBuilder withTestSeed(final int testSeed) {
		this.testSeed = testSeed;
		return this;
	}

	public PipelineEvaluationCacheConfigBuilder withValSplitTechnique(final String valSplitTechnique) {
		this.valSplitTechnique = valSplitTechnique;
		return this;
	}

	public PipelineEvaluationCacheConfigBuilder withValEvaluationTechnique(final String valEvaluationTechnique) {
		this.valEvaluationTechnique = valEvaluationTechnique;
		return this;
	}

	public PipelineEvaluationCacheConfigBuilder withValSeed(final int valSeed) {
		this.valSeed = valSeed;
		return this;
	}

	public PipelineEvaluationCacheConfigBuilder withDataset(final Instances data) {
		this.data = data;
		return this;
	}

	public PipelineEvaluationCacheConfigBuilder withSQLAdapter(final SQLAdapter sqlAdapter) {
		this.adapter = sqlAdapter;
		return this;
	}

	public String getDatasetId() {
		return this.datasetId;
	}

	public DatasetOrigin getDatasetOrigin() {
		return this.datasetOrigin;
	}

	public String getTestEvaluationTechnique() {
		return this.testEvaluationTechnique;
	}

	public String getTestSplitTechnique() {
		return this.testSplitTechnique;
	}

	public int getTestSeed() {
		return this.testSeed;
	}

	public String getValSplitTechnique() {
		return this.valSplitTechnique;
	}

	public String getValEvaluationTechnique() {
		return this.valEvaluationTechnique;
	}

	public int getValSeed() {
		return this.valSeed;
	}

	public Instances getData() {
		return this.data;
	}

	public SQLAdapter getAdapter() {
		return this.adapter;
	}

}
