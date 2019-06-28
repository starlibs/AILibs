package ai.libs.mlplan.test.cache;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.databind.ObjectMapper;

import ai.libs.hasco.exceptions.ComponentInstantiationFailedException;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.serialization.ComponentLoader;
import ai.libs.hasco.serialization.HASCOJacksonModule;
import ai.libs.jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.cache.InstructionFailedException;
import ai.libs.jaicore.ml.cache.InstructionGraph;
import ai.libs.jaicore.ml.cache.ReproducibleInstances;
import ai.libs.jaicore.ml.core.evaluation.measure.singlelabel.ZeroOneLoss;
import ai.libs.mlpipeline_evaluation.CacheEvaluatorMeasureBridge;
import ai.libs.mlplan.multiclass.wekamlplan.weka.WEKAPipelineFactory;
import weka.classifiers.Classifier;

/**
 * Test to ensure that saved {@link ReproducibleInstances} and Solutions can be
 * reproduced and create the same performance value every time.
 *
 * @author jmhansel, fmohr
 *
 */
@RunWith(Parameterized.class)
public class ReproducabilityTest {

	private static final double[] expectedResults = { 0.04548587181254307, 0.05444521019986216, 0.05513439007580979, 0.048242591316333565, 0.05651274982770503 };

	@Parameters(name = "seed = {0}")
	public static Collection<Object[]> setData(){

		Collection<Object[]> params = new ArrayList<>();
		for (int seed = 0; seed < 5; seed ++) {
			params.add(new Object[] {seed});
		}
		return params;
	}

	private int seed;

	public ReproducabilityTest(final int seed){
		this.seed = seed;
	}

	@Test
	public void testResultReproducibility() throws URISyntaxException, IOException, InstructionFailedException, InterruptedException, ComponentInstantiationFailedException, ObjectEvaluationFailedException {

		/* define serializations of data and the algorithm */
		String dataset = "[{\"name\":\"load\",\"instruction\":{\"command\":\"LoadDatasetInstructionForOpenML\",\"apiKey\":\"\",\"id\":\"40983\"},\"inputs\":[]},{\"name\":\"split\",\"instruction\":{\"command\":\"StratifiedSplitSubsetInstruction\",\"seed\": "
				+ this.seed + ",\"portionOfFirstFold\":0.7},\"inputs\":[{\"x\":\"load\",\"y\":0}]}]";
		String algorithm = "{\"component\":\"weka.classifiers.functions.SMO\",\"params\":{\"C\":\"0.1\",\"M\":\"false\",\"N\":\"1\"},\"requiredInterfaces\":{\"K\":{\"component\":\"weka.classifiers.functions.supportVector.PolyKernel\",\"params\":{\"E\":\"3\"},\"requiredInterfaces\":{}}}}";

		/* get algorithm */
		File jsonFile = Paths.get(this.getClass().getClassLoader().getResource(Paths.get("automl", "searchmodels", "weka", "weka-all-autoweka.json").toString()).toURI()).toFile();

		ComponentLoader loader = new ComponentLoader(jsonFile);
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new HASCOJacksonModule(loader.getComponents()));

		ComponentInstance composition = mapper.readValue(algorithm, ComponentInstance.class);
		InstructionGraph dsHistory = InstructionGraph.fromJson(dataset);
		ReproducibleInstances trainInstances = ReproducibleInstances.fromHistory(dsHistory, new Pair<>("split", 0));
		ReproducibleInstances validationInstances = ReproducibleInstances.fromHistory(dsHistory, new Pair<>("split", 1));
		ZeroOneLoss basicEvaluator = new ZeroOneLoss();
		CacheEvaluatorMeasureBridge bridge = new CacheEvaluatorMeasureBridge(basicEvaluator, null);
		trainInstances.setCacheLookup(false);
		validationInstances.setCacheLookup(false);
		trainInstances.setCacheStorage(false);
		validationInstances.setCacheStorage(false);
		WEKAPipelineFactory factory = new WEKAPipelineFactory();
		Classifier pipeline = factory.getComponentInstantiation(composition);
		Double score = bridge.evaluateSplit(pipeline, trainInstances, validationInstances);

		assertEquals(expectedResults[this.seed], score, 0.0001);
	}
}
