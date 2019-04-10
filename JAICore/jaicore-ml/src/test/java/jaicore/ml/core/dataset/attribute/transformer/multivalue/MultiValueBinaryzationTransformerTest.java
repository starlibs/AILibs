package jaicore.ml.core.dataset.attribute.transformer.multivalue;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.junit.Test;

import jaicore.ml.core.dataset.attribute.multivalue.MultiValueAttributeType;
import jaicore.ml.core.dataset.attribute.multivalue.MultiValueAttributeValue;

public class MultiValueBinaryzationTransformerTest {

	@Test
	public void testBinaryzation() {
		Set<String> domain = new HashSet<>();
		domain.add("a");
		domain.add("b");
		domain.add("c");
		MultiValueAttributeType type = new MultiValueAttributeType(domain);
		MultiValueBinaryzationTransformer transformer = new MultiValueBinaryzationTransformer();

		assertArrayEquals(new double[] { 0.0d, 0.0d, 0.0d },
				transformer.transformAttribute(new MultiValueAttributeValue(type, new LinkedList<>())));
		assertArrayEquals(new double[] { 1.0d, 0.0d, 0.0d },
				transformer.transformAttribute(new MultiValueAttributeValue(type, Arrays.asList("a"))));
		assertArrayEquals(new double[] { 0.0d, 1.0d, 0.0d },
				transformer.transformAttribute(new MultiValueAttributeValue(type, Arrays.asList("b"))));
		assertArrayEquals(new double[] { 0.0d, 0.0d, 1.0d },
				transformer.transformAttribute(new MultiValueAttributeValue(type, Arrays.asList("c"))));
		assertArrayEquals(new double[] { 1.0d, 1.0d, 0.0d },
				transformer.transformAttribute(new MultiValueAttributeValue(type, Arrays.asList("a", "b"))));
		assertArrayEquals(new double[] { 1.0d, 0.0d, 1.0d },
				transformer.transformAttribute(new MultiValueAttributeValue(type, Arrays.asList("a", "c"))));
		assertArrayEquals(new double[] { 0.0d, 1.0d, 1.0d },
				transformer.transformAttribute(new MultiValueAttributeValue(type, Arrays.asList("b", "c"))));
		assertArrayEquals(new double[] { 1.0d, 1.0d, 1.0d },
				transformer.transformAttribute(new MultiValueAttributeValue(type, Arrays.asList("a", "b", "c"))));
	}

}
