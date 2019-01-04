package de.upb.crc901.automl.metamining.pipelinecharacterizing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.ComponentInstanceStringConverter;
import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.WEKAOntologyConnector;
import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.WEKAPipelineCharacterizer;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import hasco.serialization.HASCOJacksonModule;
import junit.framework.Assert;
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

	String componentInstanceJSON = "{\"component\": {\"name\": \"weka.classifiers.meta.AdaBoostM1\", \"parameters\": [{\"name\": \"pActivator\", \"numeric\": false, \"categorical\": true, \"defaultValue\": \"0\", \"defaultDomain\": {\"values\": [\"0\", \"1\"]}}, {\"name\": \"Q\", \"numeric\": false, \"categorical\": true, \"defaultValue\": true, \"defaultDomain\": {\"values\": [\"true\", \"false\"]}}, {\"name\": \"S\", \"numeric\": false, \"categorical\": true, \"defaultValue\": \"1\", \"defaultDomain\": {\"values\": [\"1\"]}}, {\"name\": \"I\", \"numeric\": true, \"categorical\": false, \"defaultValue\": 10, \"defaultDomain\": {\"max\": 128, \"min\": 2, \"integer\": true}}, {\"name\": \"P\", \"numeric\": true, \"categorical\": false, \"defaultValue\": 100, \"defaultDomain\": {\"max\": 100, \"min\": 100, \"integer\": true}}], \"dependencies\": [{\"premise\": [[{\"x\": {\"name\": \"pActivator\", \"numeric\": false, \"categorical\": true, \"defaultValue\": \"0\", \"defaultDomain\": {\"values\": [\"0\", \"1\"]}}, \"y\": {\"values\": [\"0\"]}}]], \"conclusion\": [{\"x\": {\"name\": \"P\", \"numeric\": true, \"categorical\": false, \"defaultValue\": 100, \"defaultDomain\": {\"max\": 100, \"min\": 100, \"integer\": true}}, \"y\": {\"max\": 100, \"min\": 100, \"integer\": true}}]}, {\"premise\": [[{\"x\": {\"name\": \"pActivator\", \"numeric\": false, \"categorical\": true, \"defaultValue\": \"0\", \"defaultDomain\": {\"values\": [\"0\", \"1\"]}}, \"y\": {\"values\": [\"1\"]}}]], \"conclusion\": [{\"x\": {\"name\": \"P\", \"numeric\": true, \"categorical\": false, \"defaultValue\": 100, \"defaultDomain\": {\"max\": 100, \"min\": 100, \"integer\": true}}, \"y\": {\"max\": 100, \"min\": 50, \"integer\": true}}]}], \"providedInterfaces\": [\"weka.classifiers.meta.AdaBoostM1\", \"AbstractClassifier\", \"MetaClassifier\", \"BaseClassifier\"], \"requiredInterfaces\": {}}, \"parameterValues\": {\"I\": \"15\", \"P\": \"63\", \"Q\": \"true\", \"S\": \"1\", \"pActivator\": \"1\"}, \"satisfactionOfRequiredInterfaces\": {}, \"parametersThatHaveBeenSetExplicitly\": [{\"name\": \"pActivator\", \"numeric\": false, \"categorical\": true, \"defaultValue\": \"0\", \"defaultDomain\": {\"values\": [\"0\", \"1\"]}}, {\"name\": \"P\", \"numeric\": true, \"categorical\": false, \"defaultValue\": 100, \"defaultDomain\": {\"max\": 100, \"min\": 100, \"integer\": true}}, {\"name\": \"I\", \"numeric\": true, \"categorical\": false, \"defaultValue\": 10, \"defaultDomain\": {\"max\": 128, \"min\": 2, \"integer\": true}}, {\"name\": \"S\", \"numeric\": false, \"categorical\": true, \"defaultValue\": \"1\", \"defaultDomain\": {\"values\": [\"1\"]}}, {\"name\": \"Q\", \"numeric\": false, \"categorical\": true, \"defaultValue\": true, \"defaultDomain\": {\"values\": [\"true\", \"false\"]}}], \"parametersThatHaveNotBeenSetExplicitly\": []}";

	String expectedStringSerialization = "0 123 133 143 - - -";

	String expectedPatterns = "[0, 0 113 -, 0 113 - 113 -, 0 113 - 113 - 123 -, 0 113 - 123 -, 0 123 -, 0 123 114 - -, 0 123 114 107 - - -, 0 123 114 107 124 - - - -, 0 123 114 107 124 95 - - - - -, 0 123 114 107 124 97 - - - - -, 0 123 114 107 124 98 - - - - -, 0 123 114 107 124 99 - - - - -, 0 123 114 107 127 - - - -, 0 123 114 107 127 96 - - - - -, 0 123 117 - -, 0 123 117 87 - - -, 0 123 117 88 - - -, 0 123 125 - -, 0 123 133 - -, 0 123 133 141 - - -, 0 123 133 143 - - -, 107, 107 124 -, 107 124 95 - -, 107 124 97 - -, 107 124 98 - -, 107 124 99 - -, 107 127 -, 107 127 96 - -, 113, 114, 114 107 -, 114 107 124 - -, 114 107 124 95 - - -, 114 107 124 97 - - -, 114 107 124 98 - - -, 114 107 124 99 - - -, 114 107 127 - -, 114 107 127 96 - - -, 117, 117 87 -, 117 88 -, 123, 123 114 -, 123 114 107 - -, 123 114 107 124 - - -, 123 114 107 124 95 - - - -, 123 114 107 124 97 - - - -, 123 114 107 124 98 - - - -, 123 114 107 124 99 - - - -, 123 114 107 127 - - -, 123 114 107 127 96 - - - -, 123 117 -, 123 117 87 - -, 123 117 88 - -, 123 125 -, 123 133 -, 123 133 141 - -, 123 133 143 - -, 124, 124 95 -, 124 97 -, 124 98 -, 124 99 -, 125, 127, 127 96 -, 133, 133 141 -, 133 143 -, 141, 143, 87, 88, 95, 96, 97, 98, 99]";

	private static final String expectedPatternSerialization = "1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 1.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0";
	ObjectMapper mapper;

	ComponentInstance cI;

	ComponentLoader loader;

	List<String> disallowedClassifiers = Arrays.asList("weka.classifiers.meta.MultiClassClassifier",
			"weka.classifiers.meta.LogitBoost", "weka.classifiers.functions.supportVector.Puk");

	public TreeMinerSerializerTest() throws JsonParseException, JsonMappingException, IOException, URISyntaxException,
			OWLOntologyCreationException {
		File jsonFile = Paths.get(getClass().getClassLoader()
				.getResource(Paths.get("automl", "searchmodels", "weka", "weka-all-autoweka.json").toString()).toURI())
				.toFile();

		loader = new ComponentLoader(jsonFile);
		mapper = new ObjectMapper();
		mapper.registerModule(new HASCOJacksonModule(loader.getComponents()));
		cI = mapper.readValue(componentInstanceJSON, ComponentInstance.class);
	}

	/**
	 * Tests if the ComponentInstanceStringConverter works with the double labels
	 * 
	 * @throws OWLOntologyCreationException
	 */
	@Test
	public void testStringConverterConvertSingleInstance() throws OWLOntologyCreationException {
		ComponentInstanceStringConverter stringConverter = new ComponentInstanceStringConverter(
				new WEKAOntologyConnector(), Arrays.asList(cI), loader.getParamConfigs());
		Pattern pattern = Pattern.compile(" ");
		// check whether all patterns only contain doubles; will throw
		// NumberFormatException otherwise
		Assert.assertTrue(pattern.splitAsStream(stringConverter.makeStringTreeRepresentation(cI))
				.allMatch(s -> ("-".equals(s) || Double.parseDouble(s) >= 0)));
	}

	/**
	 * Checks if the WekaAlgorithm miner can load the patterns from the provided
	 * file.
	 * 
	 * @throws URISyntaxException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testTreeMinerLoadPreComputedAlgorithmPatterns()
			throws URISyntaxException, JsonParseException, JsonMappingException, IOException, InterruptedException {
		WEKAPipelineCharacterizer characterizer = new WEKAPipelineCharacterizer(loader.getParamConfigs());
		characterizer.buildFromFile();
		Assert.assertEquals(expectedPatterns, characterizer.getFoundPipelinePatterns().toString());
	}

	@Test
	public void testSingleCharacterization() {
		WEKAPipelineCharacterizer characterizer = new WEKAPipelineCharacterizer(loader.getParamConfigs());
		characterizer.buildFromFile();
		double[] characterization = characterizer.characterize(cI);

		// check if this is a correct vector
		Assert.assertTrue(Arrays.stream(characterization).allMatch(d -> d == 0.0 || d == 1.0));

		double[] unserializedArray = Pattern.compile(" ").splitAsStream(expectedPatternSerialization)
				.mapToDouble(Double::parseDouble).toArray();
		Assert.assertTrue(Arrays.equals(unserializedArray, characterization));

	}

}
