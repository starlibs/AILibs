package de.upb.crc901.mlplan.multiclass.wekamlplan.sklearn;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import hasco.model.ComponentInstance;
import jaicore.basic.FileUtil;

public class SKLearnClassifierFactoryTest {

	private static final File TEST_PIPE = new File("testrsc/sklearn/sklearn_classifier_factory.pipe_description.json");
	private static ComponentInstance componentInstance;
	private static SKLearnClassifierFactory factory;

	@BeforeClass
	public static void setup() throws IOException {
		ObjectMapper om = new ObjectMapper();
		componentInstance = om.readValue(FileUtil.readFileAsString(TEST_PIPE), ComponentInstance.class);
		factory = new SKLearnClassifierFactory();
	}

	@Test
	public void testExtractClassifier() {
		factory.extractSKLearnConstructInstruction(componentInstance);
	}

}
