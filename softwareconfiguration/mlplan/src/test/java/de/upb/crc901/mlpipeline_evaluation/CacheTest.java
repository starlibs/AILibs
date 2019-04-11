package de.upb.crc901.mlpipeline_evaluation;

import java.io.File;

import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.MLPipelineComponentInstanceFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import jaicore.basic.SQLAdapter;
import weka.attributeSelection.OneRAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.trees.RandomForest;

/**
 * An integration test for pipeline evaluations.
 *
 * @author Helena Graf
 * @author Lukas
 * @author Joshua
 *
 */
public class CacheTest {

	public static void main(final String[] args) throws Exception {
		String datasetId = "40677";
		DatasetOrigin datasetOrigin = DatasetOrigin.OPENML_DATASET_ID;
		String testEvaluationTechnique = "single";
		String testSplitTechnique = "MCCV_0.7";
		int testSeed = 123;
		String valEvaluationTechnique = "";
		String valSplitTechnique = "";
		int valSeed = 0;
		SQLAdapter adapter = new SQLAdapter("host", "user", "password", "db");

		PipelineEvaluationCacheConfigBuilder configBuilder = new PipelineEvaluationCacheConfigBuilder();

		configBuilder.withDatasetID(datasetId).withDatasetOrigin(datasetOrigin).withTestEvaluationTechnique(testEvaluationTechnique).withtestSplitTechnique(testSplitTechnique).withTestSeed(testSeed);
		configBuilder.withValEvaluationTechnique(valEvaluationTechnique).withValSplitTechnique(valSplitTechnique).withValSeed(valSeed).withSQLAdapter(adapter);

		PipelineEvaluationCache cache = new PipelineEvaluationCache(configBuilder);
		ComponentLoader loader = new ComponentLoader(new File("conf/automl/searchmodels/weka/weka-all-autoweka.json"));
		MLPipelineComponentInstanceFactory factory = new MLPipelineComponentInstanceFactory(loader.getComponents());
		ComponentInstance cI = factory.convertToComponentInstance(new MLPipeline(new Ranker(), new OneRAttributeEval(), new RandomForest()));
		cache.configureValidation("3MCCV_0.8", "multi", 12);
		System.out.println("Cache result: " + cache.getResultOrExecuteEvaluation(cI));
	}

}
