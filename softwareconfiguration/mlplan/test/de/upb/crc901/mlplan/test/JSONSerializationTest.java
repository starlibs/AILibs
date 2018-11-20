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

	String oldPlainJson = "{\"component\":{\"name\":\"weka.classifiers.trees.RandomTree\",\"requiredInterfaces\":[],\"providedInterfaces\":[\"classifier\",\"baseclassifier\"],\"parameters\":[{\"name\":\"M\",\"defaultValue\":1.0,\"min\":1.0,\"max\":10.0,\"integer\":true},{\"name\":\"K\",\"defaultValue\":0.0,\"min\":0.0,\"max\":10.0,\"integer\":true}]},\"parameterValues\":{\"K\":\"6\",\"M\":\"2\"},\"satisfactionOfRequiredInterfaces\":{}}";
	
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
