package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.junit.BeforeClass;
import org.junit.Test;

public class NominalAttributeTest extends AAttributeTest {

	private static final String ATTRIBUTE_NAME = "myNominalAttribute";
	private static final List<Integer> KEYS = Arrays.asList(0, 1, 2, 3, 4);
	private static final List<String> VALUES = Arrays.asList("a", "b", "c", "d", "e");

	private static IntBasedCategoricalAttribute attributeToTest;

	@BeforeClass
	public static void setup() {
		attributeToTest = new IntBasedCategoricalAttribute(ATTRIBUTE_NAME, VALUES);
	}

	@Override
	public String getExpectedAttributeName() {
		return ATTRIBUTE_NAME;
	}

	@Override
	public Collection<?> getValuesThatMustBeContained() {
		return KEYS;
	}

	@Override
	public IAttribute getTestedAttribute() {
		return attributeToTest;
	}

	@Test
	public void testValuesForIndices() {
		for (int index : KEYS) {
			assertEquals("Bad value for category " + index + ".", VALUES.get(index), attributeToTest.getNameOfCategory(index));
		}
	}
}
