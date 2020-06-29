package ai.libs.hasco.serialization;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.junit.Test;

import ai.libs.jaicore.basic.ResourceFile;
import ai.libs.softwareconfiguration.model.Component;
import ai.libs.softwareconfiguration.serialization.ComponentLoader;

public class ComponentLoaderTest {

	@Test
	public void testLoadFromFile() throws IOException {
		Collection<Component> components = new ComponentLoader(new File("testrsc/weka/weka-all-autoweka.json")).getComponents();
		assertTrue(!components.isEmpty());
	}

	@Test
	public void testLoadFromResource() throws IOException {
		Collection<Component> components = new ComponentLoader(new ResourceFile("ai/libs/hasco/testrsc/weka-all-autoweka.json")).getComponents();
		assertTrue(!components.isEmpty());
	}

}