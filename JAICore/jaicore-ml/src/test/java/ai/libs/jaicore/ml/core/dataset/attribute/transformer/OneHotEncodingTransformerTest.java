package ai.libs.jaicore.ml.core.dataset.attribute.transformer;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.attribute.transformer.OneHotEncodingTransformer;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttributeValue;

import ai.libs.jaicore.ml.core.dataset.attribute.CategoricalAttribute;
import ai.libs.jaicore.ml.core.dataset.attribute.CategoricalAttributeValue;

public class OneHotEncodingTransformerTest {

	private static final String[] DOMAIN_VALUES = { "A", "B", "C" };
	private static final int ATT_VALUE_IX = 1;

	public static void main(final String[] args) {
		List<String> domain = new LinkedList<>();
		Arrays.stream(DOMAIN_VALUES).forEach(domain::add);

		ICategoricalAttribute type = new CategoricalAttribute("myAtt", domain);
		ICategoricalAttributeValue value = new CategoricalAttributeValue(type, DOMAIN_VALUES[ATT_VALUE_IX]);

		double[] expected = new double[DOMAIN_VALUES.length];
		expected[ATT_VALUE_IX] = 1.0;

		System.out.println("Expected: " + Arrays.toString(expected));

		OneHotEncodingTransformer transformer = new OneHotEncodingTransformer();
		double[] actual = transformer.transformAttribute(value);
		System.out.println("Actual: " + Arrays.toString(actual));
		assertNotNull(actual);
	}

}
