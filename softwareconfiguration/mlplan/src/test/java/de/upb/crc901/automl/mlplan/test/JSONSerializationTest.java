package de.upb.crc901.automl.mlplan.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import hasco.serialization.HASCOJacksonModule;

/**
 * Tests the deserialization module for the {@link ComponentInstance} json
 * serialization.
 * 
 * @author Mirko Jürgens
 *
 */
public class JSONSerializationTest {

	private static final String allJsonFiles = "performance_stuff.json";

	/* ComponentInstance json string */
	String oldPlainJson = "{\"component\": {\"name\": \"pipeline\", \"parameters\": [], \"dependencies\": [], \"providedInterfaces\": [\"pipeline\", \"MLPipeline\", \"AbstractClassifier\"], \"requiredInterfaces\": {\"classifier\": \"BaseClassifier\", \"preprocessor\": \"AbstractPreprocessor\"}}, \"parameterValues\": {}, \"satisfactionOfRequiredInterfaces\": {\"classifier\": {\"component\": {\"name\": \"weka.classifiers.rules.ZeroR\", \"parameters\": [], \"dependencies\": [], \"providedInterfaces\": [\"weka.classifiers.rules.ZeroR\", \"AbstractClassifier\", \"WekaBaseClassifier\", \"BaseClassifier\"], \"requiredInterfaces\": {}}, \"parameterValues\": {}, \"satisfactionOfRequiredInterfaces\": {}, \"parametersThatHaveBeenSetExplicitly\": [], \"parametersThatHaveNotBeenSetExplicitly\": []}, \"preprocessor\": {\"component\": {\"name\": \"weka.attributeSelection.AttributeSelection\", \"parameters\": [{\"name\": \"M\", \"numeric\": false, \"categorical\": true, \"defaultValue\": true, \"defaultDomain\": {\"values\": [\"true\", \"false\"]}}], \"dependencies\": [], \"providedInterfaces\": [\"weka.attributeSelection.AttributeSelection\", \"AbstractPreprocessor\"], \"requiredInterfaces\": {\"eval\": \"evaluator\", \"search\": \"searcher\"}}, \"parameterValues\": {\"M\": \"false\"}, \"satisfactionOfRequiredInterfaces\": {\"eval\": {\"component\": {\"name\": \"weka.attributeSelection.SymmetricalUncertAttributeEval\", \"parameters\": [{\"name\": \"M\", \"numeric\": false, \"categorical\": true, \"defaultValue\": true, \"defaultDomain\": {\"values\": [\"true\", \"false\"]}}], \"dependencies\": [], \"providedInterfaces\": [\"weka.attributeSelection.SymmetricalUncertAttributeEval\", \"evaluator\"], \"requiredInterfaces\": {}}, \"parameterValues\": {\"M\": \"true\"}, \"satisfactionOfRequiredInterfaces\": {}, \"parametersThatHaveBeenSetExplicitly\": [{\"name\": \"M\", \"numeric\": false, \"categorical\": true, \"defaultValue\": true, \"defaultDomain\": {\"values\": [\"true\", \"false\"]}}], \"parametersThatHaveNotBeenSetExplicitly\": []}, \"search\": {\"component\": {\"name\": \"weka.attributeSelection.Ranker\", \"parameters\": [], \"dependencies\": [], \"providedInterfaces\": [\"weka.attributeSelection.Ranker\", \"searcher\"], \"requiredInterfaces\": {}}, \"parameterValues\": {}, \"satisfactionOfRequiredInterfaces\": {}, \"parametersThatHaveBeenSetExplicitly\": [], \"parametersThatHaveNotBeenSetExplicitly\": []}}, \"parametersThatHaveBeenSetExplicitly\": [{\"name\": \"M\", \"numeric\": false, \"categorical\": true, \"defaultValue\": true, \"defaultDomain\": {\"values\": [\"true\", \"false\"]}}], \"parametersThatHaveNotBeenSetExplicitly\": []}}, \"parametersThatHaveBeenSetExplicitly\": [], \"parametersThatHaveNotBeenSetExplicitly\": []}";

	@Test
	public void testSerializeComponentInstance() throws URISyntaxException, IOException {
		File jsonFile = Paths.get(getClass().getClassLoader()
				.getResource(Paths.get("automl", "searchmodels", "weka", "weka-all-autoweka.json").toString()).toURI())
				.toFile();

		ComponentLoader loader = new ComponentLoader(jsonFile);
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new HASCOJacksonModule(loader.getComponents()));

		// try to deserialize
		boolean serializationWithoutError = false;
		try {
			mapper.readValue(oldPlainJson, ComponentInstance.class);
			serializationWithoutError = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(true, serializationWithoutError);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAllComponentInstances()
			throws JsonParseException, JsonMappingException, IOException, URISyntaxException {

		File jsonFile = Paths.get(getClass().getClassLoader()
				.getResource(Paths.get("automl", "searchmodels", "weka", "weka-all-autoweka.json").toString()).toURI())
				.toFile();

		ComponentLoader loader = new ComponentLoader(jsonFile);
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new HASCOJacksonModule(loader.getComponents()));
		File allJsonFile = Paths.get(getClass().getClassLoader().getResource(allJsonFiles).toURI()).toFile();
		List<LinkedHashMap<String, String>> allCIs = mapper.readValue(allJsonFile, ArrayList.class);
		for (LinkedHashMap<String, String> content : allCIs) {
			String serializedCI = content.get("composition");

			// try to deserialize
			boolean serializationWithoutError = false;
			try {
				mapper.readValue(serializedCI, ComponentInstance.class);
				serializationWithoutError = true;
			} catch (Exception e) {
				e.printStackTrace();
			}

			assertEquals(true, serializationWithoutError);
		}

	}

}
