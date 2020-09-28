package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.Arrays;
import java.util.Collection;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.junit.jupiter.api.BeforeAll;

public class NumericAttributeTest extends AAttributeTest {

	private static final String ATTRIBUTE_NAME = "myNumericAttribute";

	private static NumericAttribute attributeToTest;

	@BeforeAll
	public static void setup() {
		attributeToTest = new NumericAttribute(ATTRIBUTE_NAME);
	}

	@Override
	public String getExpectedAttributeName() {
		return ATTRIBUTE_NAME;
	}

	@Override
	public Collection<?> getValuesThatMustBeContained() {
		return Arrays.asList(-1, 0, 1, 10, 100, 1.5, Math.sqrt(2));
	}

	@Override
	public IAttribute getTestedAttribute() {
		return attributeToTest;
	}

}
