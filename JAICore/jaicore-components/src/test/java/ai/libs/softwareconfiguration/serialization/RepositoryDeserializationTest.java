package ai.libs.softwareconfiguration.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.basic.ResourceFile;
import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.test.MediumParameterizedTest;
import ai.libs.jaicore.test.MediumTest;

public abstract class RepositoryDeserializationTest extends ATest {

	@MediumParameterizedTest
	@MethodSource("provideRepositoriesToTest")
	public void testDeserializationOfRepository(final String path, final int numExpectedComponents) throws IOException {
		logger.info("Check {} with {} components.", path, numExpectedComponents);
		ResourceFile file = new ResourceFile(path);
		IComponentRepository repo = new ComponentSerialization().deserializeRepository(file);
		assertEquals(numExpectedComponents, repo.size(), String.format("Number of components deserialized from path %s is %s instead of the expected number %s ", path, repo.size(), numExpectedComponents));
	}

}
