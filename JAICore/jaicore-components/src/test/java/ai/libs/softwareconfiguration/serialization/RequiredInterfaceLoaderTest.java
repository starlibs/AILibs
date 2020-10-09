package ai.libs.softwareconfiguration.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.components.api.IRequiredInterfaceDefinition;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.logging.LoggerUtil;

public class RequiredInterfaceLoaderTest extends ATest {

	private final JsonNode rootNode;
	private final ComponentSerialization serializer = new ComponentSerialization(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);

	public RequiredInterfaceLoaderTest() throws IOException {
		this.rootNode = new ObjectMapper().readTree(new File("testrsc/required_interface_syntax.json"));
	}

	@Test
	public void test1() throws IOException {
		IRequiredInterfaceDefinition reqInterface = this.serializer.deserializeRequiredInterface(this.rootNode.path("requiredInterface").get(0));
		assertTrue(reqInterface.isOptional());
		assertEquals(1, reqInterface.getMin());
		assertEquals(1, reqInterface.getMax());
		assertTrue(reqInterface.isOrdered());
		assertFalse(reqInterface.isUniqueComponents());
	}

	@Test
	public void test2() throws IOException {
		IRequiredInterfaceDefinition reqInterface = this.serializer.deserializeRequiredInterface(this.rootNode.path("requiredInterface").get(1));
		assertEquals(1, reqInterface.getMin());
		assertEquals(1, reqInterface.getMax());
		assertTrue(reqInterface.isOrdered());
		assertFalse(reqInterface.isUniqueComponents());
	}

	@Test
	public void test3() throws IOException {
		IRequiredInterfaceDefinition reqInterface = this.serializer.deserializeRequiredInterface(this.rootNode.path("requiredInterface").get(2));
		assertEquals(1, reqInterface.getMin());
		assertEquals(1, reqInterface.getMax());
		assertTrue(reqInterface.isOrdered());
		assertFalse(reqInterface.isUniqueComponents());
	}

	@Test
	public void test4() throws IOException {
		IRequiredInterfaceDefinition reqInterface = this.serializer.deserializeRequiredInterface(this.rootNode.path("requiredInterface").get(3));
		assertEquals(0, reqInterface.getMin());
		assertEquals(1, reqInterface.getMax());
		assertTrue(reqInterface.isOrdered());
		assertFalse(reqInterface.isUniqueComponents());
	}

	@Test
	public void test5() throws IOException {
		JsonNode node = this.rootNode.path("requiredInterface").get(4);
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			this.serializer.deserializeRequiredInterface(node);
		});
	}

	@Test
	public void test6() throws IOException {
		IRequiredInterfaceDefinition reqInterface = this.serializer.deserializeRequiredInterface(this.rootNode.path("requiredInterface").get(5));
		assertEquals(2, reqInterface.getMin());
		assertEquals(6, reqInterface.getMax());
		assertTrue(reqInterface.isOrdered());
		assertFalse(reqInterface.isUniqueComponents());
	}

	@Test
	public void test7() throws IOException {
		IRequiredInterfaceDefinition reqInterface = this.serializer.deserializeRequiredInterface(this.rootNode.path("requiredInterface").get(6));
		assertEquals(2, reqInterface.getMin());
		assertEquals(2, reqInterface.getMax());
		assertTrue(reqInterface.isOrdered());
		assertFalse(reqInterface.isUniqueComponents());
	}

	@Test
	public void test8() throws IOException {
		IRequiredInterfaceDefinition reqInterface = this.serializer.deserializeRequiredInterface(this.rootNode.path("requiredInterface").get(7));
		assertEquals(1, reqInterface.getMin());
		assertEquals(2, reqInterface.getMax());
		assertTrue(reqInterface.isOrdered());
		assertFalse(reqInterface.isUniqueComponents());
	}

	@Test
	public void test9() throws IOException {
		IRequiredInterfaceDefinition reqInterface = this.serializer.deserializeRequiredInterface(this.rootNode.path("requiredInterface").get(8));
		assertTrue(reqInterface.isOptional());
		assertEquals(1, reqInterface.getMin());
		assertEquals(2, reqInterface.getMax());
		assertTrue(reqInterface.isOrdered());
		assertFalse(reqInterface.isUniqueComponents());
	}

	@Test
	public void test10() throws IOException {
		IRequiredInterfaceDefinition reqInterface = this.serializer.deserializeRequiredInterface(this.rootNode.path("requiredInterface").get(9));
		assertTrue(reqInterface.isOptional());
		assertEquals(2, reqInterface.getMin());
		assertEquals(2, reqInterface.getMax());
		assertTrue(reqInterface.isOrdered());
		assertFalse(reqInterface.isUniqueComponents());
	}

	@Test
	public void test11() throws IOException {
		IRequiredInterfaceDefinition reqInterface = this.serializer.deserializeRequiredInterface(this.rootNode.path("requiredInterface").get(10));
		assertFalse(reqInterface.isOptional());
		assertEquals(2, reqInterface.getMin());
		assertEquals(2, reqInterface.getMax());
		assertFalse(reqInterface.isOrdered());
		assertTrue(reqInterface.isUniqueComponents());
	}
}
