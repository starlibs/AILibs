package de.upb.crc901.mlplan.test;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import hasco.serialization.HASCOJacksonModule;
import junit.framework.Assert;

public class JSONSerializationTest {

	String oldPlainJson = "{\"component\": {\"name\": \"pipeline\", \"parameters\": [], \"dependencies\": [], \"providedInterfaces\": [\"MLPipeline\", \"AbstractClassifier\"], \"requiredInterfaces\": {\"classifier\": \"BaseClassifier\", \"preprocessor\": \"AbstractPreprocessor\"}}, \"parameterValues\": {}, \"satisfactionOfRequiredInterfaces\": {\"classifier\": {\"component\": {\"name\": \"weka.classifiers.trees.RandomForest\", \"parameters\": [{\"name\": \"depthActivator\", \"defaultValue\": \"0\", \"defaultDomain\": {\"type\": \"categorical\", \"values\": [\"0\", \"1\"]}}, {\"name\": \"featuresActivator\", \"defaultValue\": \"0\", \"defaultDomain\": {\"type\": \"categorical\", \"values\": [\"0\", \"1\"]}}, {\"name\": \"depth\", \"defaultValue\": 0, \"defaultDomain\": {\"max\": 0, \"min\": 0, \"type\": \"numeric\", \"integer\": true}}, {\"name\": \"I\", \"defaultValue\": 10, \"defaultDomain\": {\"max\": 256, \"min\": 2, \"type\": \"numeric\", \"integer\": true}}, {\"name\": \"K\", \"defaultValue\": 0, \"defaultDomain\": {\"max\": 0, \"min\": 0, \"type\": \"numeric\", \"integer\": true}}], \"dependencies\": [{\"premise\": [[{\"x\": {\"name\": \"featuresActivator\", \"defaultValue\": \"0\", \"defaultDomain\": {\"type\": \"categorical\", \"values\": [\"0\", \"1\"]}}, \"y\": {\"type\": \"categorical\", \"values\": [\"0\"]}}]], \"conclusion\": [{\"x\": {\"name\": \"K\", \"defaultValue\": 0, \"defaultDomain\": {\"max\": 0, \"min\": 0, \"type\": \"numeric\", \"integer\": true}}, \"y\": {\"max\": 0, \"min\": 0, \"type\": \"numeric\", \"integer\": true}}]}, {\"premise\": [[{\"x\": {\"name\": \"featuresActivator\", \"defaultValue\": \"0\", \"defaultDomain\": {\"type\": \"categorical\", \"values\": [\"0\", \"1\"]}}, \"y\": {\"type\": \"categorical\", \"values\": [\"1\"]}}]], \"conclusion\": [{\"x\": {\"name\": \"K\", \"defaultValue\": 0, \"defaultDomain\": {\"max\": 0, \"min\": 0, \"type\": \"numeric\", \"integer\": true}}, \"y\": {\"max\": 32, \"min\": 1, \"type\": \"numeric\", \"integer\": true}}]}, {\"premise\": [[{\"x\": {\"name\": \"depthActivator\", \"defaultValue\": \"0\", \"defaultDomain\": {\"type\": \"categorical\", \"values\": [\"0\", \"1\"]}}, \"y\": {\"type\": \"categorical\", \"values\": [\"0\"]}}]], \"conclusion\": [{\"x\": {\"name\": \"depth\", \"defaultValue\": 0, \"defaultDomain\": {\"max\": 0, \"min\": 0, \"type\": \"numeric\", \"integer\": true}}, \"y\": {\"max\": 0, \"min\": 0, \"type\": \"numeric\", \"integer\": true}}]}, {\"premise\": [[{\"x\": {\"name\": \"depthActivator\", \"defaultValue\": \"0\", \"defaultDomain\": {\"type\": \"categorical\", \"values\": [\"0\", \"1\"]}}, \"y\": {\"type\": \"categorical\", \"values\": [\"1\"]}}]], \"conclusion\": [{\"x\": {\"name\": \"depth\", \"defaultValue\": 0, \"defaultDomain\": {\"max\": 0, \"min\": 0, \"type\": \"numeric\", \"integer\": true}}, \"y\": {\"max\": 20, \"min\": 1, \"type\": \"numeric\", \"integer\": true}}]}], \"providedInterfaces\": [\"AbstractClassifier\", \"WekaBaseClassifier\", \"BaseClassifier\"], \"requiredInterfaces\": {}}, \"parameterValues\": {\"I\": \"6\", \"K\": \"0\", \"depth\": \"0\", \"depthActivator\": \"0\", \"featuresActivator\": \"0\"}, \"satisfactionOfRequiredInterfaces\": {}}, \"preprocessor\": {\"component\": {\"name\": \"weka.attributeSelection.AttributeSelection\", \"parameters\": [{\"name\": \"M\", \"defaultValue\": true, \"defaultDomain\": {\"type\": \"boolean\", \"values\": [\"true\", \"false\"]}}], \"dependencies\": [], \"providedInterfaces\": [\"AbstractPreprocessor\"], \"requiredInterfaces\": {\"eval\": \"evaluator\", \"search\": \"searcher\"}}, \"parameterValues\": {\"M\": \"true\"}, \"satisfactionOfRequiredInterfaces\": {\"eval\": {\"component\": {\"name\": \"weka.attributeSelection.OneRAttributeEval\", \"parameters\": [{\"name\": \"B\", \"defaultValue\": 6, \"defaultDomain\": {\"max\": 64, \"min\": 1, \"type\": \"numeric\", \"integer\": true}}, {\"name\": \"D\", \"defaultValue\": true, \"defaultDomain\": {\"type\": \"boolean\", \"values\": [\"true\", \"false\"]}}, {\"name\": \"F\", \"defaultValue\": 10, \"defaultDomain\": {\"max\": 15, \"min\": 2, \"type\": \"numeric\", \"integer\": true}}, {\"name\": \"S\", \"defaultValue\": \"0\", \"defaultDomain\": {\"type\": \"categorical\", \"values\": [\"0\"]}}], \"dependencies\": [], \"providedInterfaces\": [\"evaluator\"], \"requiredInterfaces\": {}}, \"parameterValues\": {\"B\": \"36\", \"D\": \"true\", \"F\": \"11\", \"S\": \"0\"}, \"satisfactionOfRequiredInterfaces\": {}}, \"search\": {\"component\": {\"name\": \"weka.attributeSelection.Ranker\", \"parameters\": [], \"dependencies\": [], \"providedInterfaces\": [\"searcher\"], \"requiredInterfaces\": {}}, \"parameterValues\": {}, \"satisfactionOfRequiredInterfaces\": {}}}}}}";
	String path = "/Users/elppa/Desktop/weka-test.json";
	
	@Test
	public void testSerializeComponentInstance() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new HASCOJacksonModule());

		try {
			ComponentInstance instance = mapper.readValue(oldPlainJson, ComponentInstance.class);
			System.out.println(instance);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testCOmponentLoader() throws IOException {
		ComponentLoader loader = new ComponentLoader(new File(path));
		System.out.println(loader.getComponents());
	}
}
