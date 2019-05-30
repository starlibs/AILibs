package jaicore.ml.core.dataset.attribute.transformer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;

public class OneHotEncodingTransformerTest {

	private static final String[] DOMAIN_VALUES = { "A", "B", "C" };
	private static final int ATT_VALUE_IX = 1;

	public static void main(final String[] args) {
		List<String> domain = new LinkedList<>();
		Arrays.stream(DOMAIN_VALUES).forEach(domain::add);

		CategoricalAttributeType type = new CategoricalAttributeType(domain);
		CategoricalAttributeValue value = new CategoricalAttributeValue(type, DOMAIN_VALUES[ATT_VALUE_IX]);

		double[] expected = new double[DOMAIN_VALUES.length];
		expected[ATT_VALUE_IX] = 1.0;

		System.out.println("Expected: " + Arrays.toString(expected));

		OneHotEncodingTransformer transformer = new OneHotEncodingTransformer();
		double[] actual = transformer.transformAttribute(value);

		System.out.println("Actual: " + Arrays.toString(actual));
	}

}
