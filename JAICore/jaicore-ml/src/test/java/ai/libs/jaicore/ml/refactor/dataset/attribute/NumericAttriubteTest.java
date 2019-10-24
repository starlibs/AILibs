package ai.libs.jaicore.ml.refactor.dataset.attribute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttributeValue;

public class NumericAttriubteTest {

	private static final String NAME = "myNumericAttribute";
	private static final int SAMPLES = 10;

	private NumericAttribute attribute;

	private Random rand = new Random(0);

	@Before
	public void setup() {
		this.attribute = new NumericAttribute(NAME);
	}

	@Test
	public void testAttributeName() {
		assertEquals("Name of attribute is wrong", NAME, this.attribute.getName());
	}

	@Test
	public void testValidValues() {
		for (int i = 0; i < SAMPLES; i++) {
			double value = this.rand.nextDouble();
			assertTrue("Valid double value has not been recognized.", this.attribute.isValidValue(value));
			assertTrue("Valid double value has not been recognized.", this.attribute.isValidValue(new NumericAttributeValue(value)));

			int intValue = this.rand.nextInt();
			assertTrue("Valid int value has not been recognized.", this.attribute.isValidValue(intValue));
			assertTrue("Valid int value has not been recognized.", this.attribute.isValidValue(new NumericAttributeValue(intValue)));
		}
	}

	@Test
	public void testEncodedValue() {
		for (int i = 0; i < SAMPLES; i++) {
			double value = this.rand.nextDouble();
			assertEquals("Valid double value has not been recognized.", value, this.attribute.encodeValue(value), 0);

			int intValue = this.rand.nextInt();
			assertEquals("Valid int value has not been recognized.", intValue, this.attribute.encodeValue(intValue), 0);
		}
	}

	@Test
	public void testDecodedValue() {
		for (int i = 0; i < SAMPLES; i++) {
			double value = this.rand.nextDouble();
			assertEquals("Valid double value has not been recognized.", value, this.attribute.decodeValue(value), 0);

			int intValue = this.rand.nextInt();
			assertEquals("Valid int value has not been recognized.", intValue, this.attribute.decodeValue(intValue), 0);
		}
	}

}
