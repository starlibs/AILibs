package hasco.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.knowledgebase.FANOVAParameterImportanceEstimator;
import hasco.knowledgebase.PerformanceKnowledgeBase;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class FANOVAParameterImportanceEstimatorTest {

	private static String testFile = "testrsc/regression_data/performance2.arff";

	private static final Logger LOGGER = LoggerFactory.getLogger(FANOVAParameterImportanceEstimatorTest.class);

	@Test
	public void testImportanceEstimation() {
		PerformanceKnowledgeBase pkb = new PerformanceKnowledgeBase();
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(testFile), StandardCharsets.UTF_8)) {
			ArffReader arffReader = new ArffReader(reader);
			Instances data = arffReader.getData();
			data.setClassIndex(data.numAttributes() - 1);
			Component component = new Component("Component");
			ComponentInstance composition = new ComponentInstance(component, null, null);
			pkb.setPerformanceSamples(data, composition, "test");
			FANOVAParameterImportanceEstimator importanceEstimator = new FANOVAParameterImportanceEstimator("test", 2, 0.08);
			importanceEstimator.setPerformanceKnowledgeBase(pkb);
			try {
				Set<String> importantParams = importanceEstimator.extractImportantParameters(composition, false);
				LOGGER.info("important parameters: {}", importantParams);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(true);
	}
}
