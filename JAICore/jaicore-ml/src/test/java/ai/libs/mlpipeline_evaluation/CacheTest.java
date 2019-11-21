package ai.libs.mlpipeline_evaluation;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.serialization.ComponentLoader;
import ai.libs.jaicore.basic.SQLAdapter;
import ai.libs.jaicore.ml.experiments.DatasetOrigin;
import ai.libs.jaicore.ml.experiments.PipelineEvaluationCache;
import ai.libs.jaicore.ml.experiments.PipelineEvaluationCacheConfigBuilder;
import ai.libs.mlplan.multiclass.wekamlplan.weka.MLPipelineComponentInstanceFactory;
import ai.libs.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
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

	@Test
	public void testCache() throws Exception {
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
		assertNotNull(cache.getResultOrExecuteEvaluation(cI));
	}

}
