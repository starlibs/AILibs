package ai.libs.jaicore.ml.dataset;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import ai.libs.jaicore.ml.core.tabular.dataset.attribute.CategoricalAttribute;

public class NominalAttributeTest {

	private static final String ATTRIBUTE_NAME = "myNominalAttribute";
	private static final List<String> VALUES = Arrays.asList("a", "b", "c", "d", "e");

	private static final String ELEMENT = "c";
	private static final double ELEMENT_VALUE = 2.0;

	private static CategoricalAttribute attributeToTest;

	@BeforeClass
	public static void setup() {
		attributeToTest = new CategoricalAttribute(ATTRIBUTE_NAME, VALUES);
	}

	@Test
	public void testNameOfAttribute() {
		assertEquals("The returned name of the attribute does not match the defined one.", ATTRIBUTE_NAME, attributeToTest.getName());
	}

	@Test
	public void testValues() {
		assertEquals("The internal values in the attribute do not match the defined ones.", VALUES, attributeToTest.getValues());
	}

	@Test
	public void testgetValueID() {
		assertEquals("The returned value id does not match the expected one", ELEMENT_VALUE, attributeToTest.getValueID(ELEMENT), 0.00001);
	}

}
