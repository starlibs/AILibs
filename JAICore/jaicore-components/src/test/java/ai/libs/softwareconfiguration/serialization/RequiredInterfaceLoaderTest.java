package ai.libs.softwareconfiguration.serialization;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.components.api.IRequiredInterfaceDefinition;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.logging.LoggerUtil;

public class RequiredInterfaceLoaderTest extends ATest {

	private static JsonNode rootNode;
	private static ComponentSerialization serializer = new ComponentSerialization(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);

	@BeforeAll
	public static void setup() throws IOException {
		rootNode = new ObjectMapper().readTree(new File("testrsc/required_interface_syntax.json"));
	}

	class RequiredInterfaceTestCase {

		private int min;
		private int max;
		private boolean ordered;
		private boolean optional;
		private boolean uniqueComponents;

		private RequiredInterfaceTestCase(final boolean optional, final int min, final int max, final boolean ordered, final boolean uniqueComponents) {
			this.optional = optional;
			this.min = min;
			this.max = max;
		}

	}

	@ParameterizedTest
	@MethodSource("getRequiredInterfaceTestCases")
	public void testDeserializeRequiredInterface(final IRequiredInterfaceDefinition reqInterface, final boolean optional, final int min, final int max, final boolean ordered, final boolean uniqueComponents) {
		assertEquals(optional, reqInterface.isOptional());
		assertEquals(min, reqInterface.getMin());
		assertEquals(max, reqInterface.getMax());
		assertEquals(ordered, reqInterface.isOrdered());
		assertEquals(uniqueComponents, reqInterface.isUniqueComponents());
	}

	public static Stream<Arguments> getRequiredInterfaceTestCases() {
		return Stream.of( //
				Arguments.of(serializer.deserializeRequiredInterface(rootNode.path("requiredInterface").get(0)), true, 1, 1, true, false), // test case 1
				Arguments.of(serializer.deserializeRequiredInterface(rootNode.path("requiredInterface").get(1)), false, 1, 1, true, false), // test case 2
				Arguments.of(serializer.deserializeRequiredInterface(rootNode.path("requiredInterface").get(2)), false, 1, 1, true, false), // test case 3
				Arguments.of(serializer.deserializeRequiredInterface(rootNode.path("requiredInterface").get(3)), false, 0, 1, true, false), // test case 4
				Arguments.of(serializer.deserializeRequiredInterface(rootNode.path("requiredInterface").get(5)), false, 2, 6, true, false), // test case 6
				Arguments.of(serializer.deserializeRequiredInterface(rootNode.path("requiredInterface").get(6)), false, 2, 2, true, false), // test case 7
				Arguments.of(serializer.deserializeRequiredInterface(rootNode.path("requiredInterface").get(7)), false, 1, 2, true, false), // test case 8
				Arguments.of(serializer.deserializeRequiredInterface(rootNode.path("requiredInterface").get(8)), true, 1, 2, true, false), // test case 9
				Arguments.of(serializer.deserializeRequiredInterface(rootNode.path("requiredInterface").get(9)), true, 2, 2, true, false), // test case 10
				Arguments.of(serializer.deserializeRequiredInterface(rootNode.path("requiredInterface").get(10)), false, 2, 2, false, true) // test case 11
		);
	}

	@Test
	public void testFailingDeserialization() throws IOException {
		JsonNode node = rootNode.path("requiredInterface").get(4);
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			serializer.deserializeRequiredInterface(node);
		});
	}
}
