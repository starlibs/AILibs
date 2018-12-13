package de.upb.crc901.automl.metamining.pipelinecharacterizing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.ComponentInstanceStringConverter;
import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.WEKAOntologyConnector;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import hasco.serialization.HASCOJacksonModule;
import treeminer.TreeMiner;

/**
 * This tests the functionalities of the
 * {@link ComponentInstanceStringConverter} which makes extensive use of the
 * provided {@link TreeMiner}.
 * 
 * In particular, this tests whether or not weka labels are correctly replaced
 * by integer values (mainly performance reasons). And if the TreeMiner
 * representations of Meta-Features are correctly serialized.
 * 
 * @author Mirko Jürgens
 *
 */
public class TreeMinerSerializerTest {
	String componentInstanceJSON = "{\"component\": {\"name\": \"weka.classifiers.rules.OneR\", \"parameters\": [{\"name\": \"B\", \"numeric\": true, \"categorical\": false, \"defaultValue\": 6, \"defaultDomain\": {\"max\": 32, \"min\": 1, \"integer\": true}}], \"dependencies\": [], \"providedInterfaces\": [\"weka.classifiers.rules.OneR\", \"AbstractClassifier\", \"WekaBaseClassifier\", \"BaseClassifier\"], \"requiredInterfaces\": {}}, \"parameterValues\": {\"B\": \"11\"}, \"satisfactionOfRequiredInterfaces\": {}, \"parametersThatHaveBeenSetExplicitly\": [{\"name\": \"B\", \"numeric\": true, \"categorical\": false, \"defaultValue\": 6, \"defaultDomain\": {\"max\": 32, \"min\": 1, \"integer\": true}}], \"parametersThatHaveNotBeenSetExplicitly\": []}";

	ObjectMapper mapper;

	ComponentInstance cI;

	ComponentLoader loader;

	public TreeMinerSerializerTest() throws JsonParseException, JsonMappingException, IOException, URISyntaxException {
		mapper = new ObjectMapper();
		mapper.registerModule(new HASCOJacksonModule());
		cI = mapper.readValue(componentInstanceJSON, ComponentInstance.class);
		File jsonFile = Paths.get(getClass().getClassLoader()
				.getResource(Paths.get("automl", "searchmodels", "weka", "weka-all-autoweka.json").toString()).toURI())
				.toFile();
		loader = new ComponentLoader(jsonFile);
	}

	@Test
	public void testCIStringConverter() throws OWLOntologyCreationException {
		ComponentInstanceStringConverter stringConverter = new ComponentInstanceStringConverter(
				new WEKAOntologyConnector(), Arrays.asList(cI), loader.getParamConfigs());
		System.out.println(stringConverter.makeStringTreeRepresentation(cI));
	}
}
