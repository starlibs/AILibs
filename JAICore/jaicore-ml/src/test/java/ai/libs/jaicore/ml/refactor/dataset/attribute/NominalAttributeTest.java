package ai.libs.jaicore.ml.refactor.dataset.attribute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ai.libs.jaicore.ml.core.tabular.dataset.attribute.CategoricalAttribute;
import ai.libs.jaicore.ml.core.tabular.dataset.attribute.CategoricalAttributeValue;

public class NominalAttributeTest {

	private static final String NAME = "myNominalAttribute";
	private static final List<String> DOMAIN_VALUES = Arrays.asList("A", "B", "C", "D");

	private CategoricalAttribute attribute;

	@Before
	public void setup() {
		this.attribute = new CategoricalAttribute(NAME, DOMAIN_VALUES);
	}

	@Test
	public void testAttributeName() {
		assertEquals("Name of attribute is wrong", NAME, this.attribute.getName());
	}

	@Test
	public void testValidValues() {
		for (String value : DOMAIN_VALUES) {
			assertTrue("Valid values has not been recognized.", this.attribute.isValidValue(value));
			assertTrue("Valid values has not been recognized.", this.attribute.isValidValue(new CategoricalAttributeValue(value)));
		}
	}

	@Test
	public void testEncodedValue() {
		for (String value : DOMAIN_VALUES) {
			assertEquals("Double encoding not as expected", DOMAIN_VALUES.indexOf(value) + 1, this.attribute.encodeValue(value), 0);
		}
	}

	@Test
	public void testDecodedValue() {
		for (int i = 0; i < DOMAIN_VALUES.size(); i++) {
			assertEquals("Double decoding not as expected", DOMAIN_VALUES.get(i), this.attribute.decodeValue(i + 1));
		}
	}

}
