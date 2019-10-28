package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttribute;

public class NumericAttributeTest {

	private static final String ATTRIBUTE_NAME = "myNumericAttribute";

	private static NumericAttribute attributeToTest;

	@BeforeClass
	public static void setup() {
		attributeToTest = new NumericAttribute(ATTRIBUTE_NAME);
	}

	@Test
	public void testNameOfAttribute() {
		assertEquals("The returned name of the attribute does not match the defined one.", ATTRIBUTE_NAME, attributeToTest.getName());
	}

}
