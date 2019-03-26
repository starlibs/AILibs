package de.upb.crc901.automl.mlplan.test;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import hasco.model.ComponentInstance;
import hasco.serialization.HASCOJacksonModule;
import junit.framework.Assert;
/**
 * Tests the deserialization module for the {@link ComponentInstance} json serialization.
 * @author elppa
 *
 */
public class JSONSerializationTest {

	/* ComponentInstance json string*/
	String oldPlainJson = "{\"component\": {\"name\": \"pipeline\", \"parameters\": [], \"dependencies\": [], \"providedInterfaces\": [\"pipeline\", \"MLPipeline\", \"AbstractClassifier\"], \"requiredInterfaces\": {\"classifier\": \"BaseClassifier\", \"preprocessor\": \"AbstractPreprocessor\"}}, \"parameterValues\": {}, \"satisfactionOfRequiredInterfaces\": {\"classifier\": {\"component\": {\"name\": \"weka.classifiers.rules.ZeroR\", \"parameters\": [], \"dependencies\": [], \"providedInterfaces\": [\"weka.classifiers.rules.ZeroR\", \"AbstractClassifier\", \"WekaBaseClassifier\", \"BaseClassifier\"], \"requiredInterfaces\": {}}, \"parameterValues\": {}, \"satisfactionOfRequiredInterfaces\": {}, \"parametersThatHaveBeenSetExplicitly\": [], \"parametersThatHaveNotBeenSetExplicitly\": []}, \"preprocessor\": {\"component\": {\"name\": \"weka.attributeSelection.AttributeSelection\", \"parameters\": [{\"name\": \"M\", \"numeric\": false, \"categorical\": true, \"defaultValue\": true, \"defaultDomain\": {\"values\": [\"true\", \"false\"]}}], \"dependencies\": [], \"providedInterfaces\": [\"weka.attributeSelection.AttributeSelection\", \"AbstractPreprocessor\"], \"requiredInterfaces\": {\"eval\": \"evaluator\", \"search\": \"searcher\"}}, \"parameterValues\": {\"M\": \"false\"}, \"satisfactionOfRequiredInterfaces\": {\"eval\": {\"component\": {\"name\": \"weka.attributeSelection.SymmetricalUncertAttributeEval\", \"parameters\": [{\"name\": \"M\", \"numeric\": false, \"categorical\": true, \"defaultValue\": true, \"defaultDomain\": {\"values\": [\"true\", \"false\"]}}], \"dependencies\": [], \"providedInterfaces\": [\"weka.attributeSelection.SymmetricalUncertAttributeEval\", \"evaluator\"], \"requiredInterfaces\": {}}, \"parameterValues\": {\"M\": \"true\"}, \"satisfactionOfRequiredInterfaces\": {}, \"parametersThatHaveBeenSetExplicitly\": [{\"name\": \"M\", \"numeric\": false, \"categorical\": true, \"defaultValue\": true, \"defaultDomain\": {\"values\": [\"true\", \"false\"]}}], \"parametersThatHaveNotBeenSetExplicitly\": []}, \"search\": {\"component\": {\"name\": \"weka.attributeSelection.Ranker\", \"parameters\": [], \"dependencies\": [], \"providedInterfaces\": [\"weka.attributeSelection.Ranker\", \"searcher\"], \"requiredInterfaces\": {}}, \"parameterValues\": {}, \"satisfactionOfRequiredInterfaces\": {}, \"parametersThatHaveBeenSetExplicitly\": [], \"parametersThatHaveNotBeenSetExplicitly\": []}}, \"parametersThatHaveBeenSetExplicitly\": [{\"name\": \"M\", \"numeric\": false, \"categorical\": true, \"defaultValue\": true, \"defaultDomain\": {\"values\": [\"true\", \"false\"]}}], \"parametersThatHaveNotBeenSetExplicitly\": []}}, \"parametersThatHaveBeenSetExplicitly\": [], \"parametersThatHaveNotBeenSetExplicitly\": []}"; 
			
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
	
}
