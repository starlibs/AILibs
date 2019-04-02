package de.upb.crc901.mlplan.test.cache;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.upb.crc901.mlpipeline_evaluation.CacheEvaluatorMeasureBridge;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WEKAPipelineFactory;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import hasco.serialization.HASCOJacksonModule;
import jaicore.ml.cache.Instruction;
import jaicore.ml.cache.ReproducibleInstances;
import jaicore.ml.core.evaluation.measure.singlelabel.ZeroOneLoss;
import weka.classifiers.Classifier;

/**
 * Test to ensure that saved {@link ReproducibleInstances} and Solutions can be
 * reproduced and create the same performance value every time.
 * 
 * @author jmhansel
 *
 */
public class ReproducabilityTest {

	@Test
	public void test() throws URISyntaxException, IOException {
		String trainJson = "[{\"inputs\": {\"id\": \"40983\", \"provider\": \"openml.org\"}, \"command\": \"loadDataset\"}, {\"inputs\": {\"seed\": \"-4962768465676381896\", \"ratios\": \"[0.7]\", \"outIndex\": \"0\"}, \"command\": \"split\"}, {\"inputs\": {\"seed\": \"0\", \"ratios\": \"[0.3]\", \"outIndex\": \"1\"}, \"command\": \"split\"}, {\"inputs\": {\"seed\": \"1534718591\", \"ratios\": \"[0.7]\", \"outIndex\": \"0\"}, \"command\": \"split\"}]";
		String validationJson = "[{\"inputs\": {\"id\": \"40983\", \"provider\": \"openml.org\"}, \"command\": \"loadDataset\"}, {\"inputs\": {\"seed\": \"-4962768465676381896\", \"ratios\": \"[0.7]\", \"outIndex\": \"0\"}, \"command\": \"split\"}, {\"inputs\": {\"seed\": \"0\", \"ratios\": \"[0.3]\", \"outIndex\": \"1\"}, \"command\": \"split\"}, {\"inputs\": {\"seed\": \"1534718591\", \"ratios\": \"[0.7]\", \"outIndex\": \"1\"}, \"command\": \"split\"}]";
		String compositionJson = "{\"component\": {\"name\": \"weka.classifiers.bayes.NaiveBayesMultinomial\", \"parameters\": [], \"dependencies\": [], \"providedInterfaces\": [\"weka.classifiers.bayes.NaiveBayesMultinomial\", \"AbstractClassifier\", \"WekaBaseClassifier\", \"BaseClassifier\"], \"requiredInterfaces\": {}}, \"parameterValues\": {}, \"satisfactionOfRequiredInterfaces\": {}, \"parametersThatHaveBeenSetExplicitly\": [], \"parametersThatHaveNotBeenSetExplicitly\": []}";

		File jsonFile = Paths.get(getClass().getClassLoader()
				.getResource(Paths.get("automl", "searchmodels", "weka", "weka-all-autoweka.json").toString()).toURI())
				.toFile();

		ComponentLoader loader = new ComponentLoader(jsonFile);
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new HASCOJacksonModule(loader.getComponents()));

		try {
			List<Instruction> trainHistory = mapper.readValue(trainJson, new TypeReference<List<Instruction>>() {
			});
			List<Instruction> validationHistory = mapper.readValue(validationJson,
					new TypeReference<List<Instruction>>() {
					});
			ComponentInstance composition = mapper.readValue(compositionJson, ComponentInstance.class);
			ReproducibleInstances trainInstances = ReproducibleInstances.fromHistory(trainHistory,
					"4350e421cdc16404033ef1812ea38c01");
			ReproducibleInstances validationInstances = ReproducibleInstances.fromHistory(validationHistory,
					"4350e421cdc16404033ef1812ea38c01");
			ZeroOneLoss basicEvaluator = new ZeroOneLoss();
			CacheEvaluatorMeasureBridge bridge = new CacheEvaluatorMeasureBridge(basicEvaluator, null);
			trainInstances.setCacheLookup(false);
			validationInstances.setCacheLookup(false);
			trainInstances.setCacheStorage(false);
			validationInstances.setCacheStorage(false);
			WEKAPipelineFactory factory = new WEKAPipelineFactory();
			Classifier pipeline = factory.getComponentInstantiation(composition);
			Double score = bridge.evaluateSplit(pipeline, trainInstances, validationInstances);
			
			assertEquals(0.2909604519774011, score, 0.0001);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
