package ai.libs.jaicore.ml.core.olddataset.attribute.transformer.multivalue;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.junit.Test;

import ai.libs.jaicore.ml.core.dataset.schema.attribute.MultiValueAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.MultiValueAttributeValue;
import ai.libs.jaicore.ml.core.olddataset.attributetransformer.multivalue.MultiValueBinarizationTransformer;

public class MultiValueBinaryzationTransformerTest {
	private static final double DELTA = 0.00001;

	@Test
	public void testBinaryzation() {
		Set<String> domain = new HashSet<>();
		domain.add("a");
		domain.add("b");
		domain.add("c");
		MultiValueAttribute type = new MultiValueAttribute("", domain);
		MultiValueBinarizationTransformer transformer = new MultiValueBinarizationTransformer();

		assertArrayEquals(new double[] { 0.0d, 0.0d, 0.0d }, transformer.transformAttribute(new MultiValueAttributeValue(type, new LinkedList<>())), DELTA);
		assertArrayEquals(new double[] { 1.0d, 0.0d, 0.0d }, transformer.transformAttribute(new MultiValueAttributeValue(type, Arrays.asList("a"))), DELTA);
		assertArrayEquals(new double[] { 0.0d, 1.0d, 0.0d }, transformer.transformAttribute(new MultiValueAttributeValue(type, Arrays.asList("b"))), DELTA);
		assertArrayEquals(new double[] { 0.0d, 0.0d, 1.0d }, transformer.transformAttribute(new MultiValueAttributeValue(type, Arrays.asList("c"))), DELTA);
		assertArrayEquals(new double[] { 1.0d, 1.0d, 0.0d }, transformer.transformAttribute(new MultiValueAttributeValue(type, Arrays.asList("a", "b"))), DELTA);
		assertArrayEquals(new double[] { 1.0d, 0.0d, 1.0d }, transformer.transformAttribute(new MultiValueAttributeValue(type, Arrays.asList("a", "c"))), DELTA);
		assertArrayEquals(new double[] { 0.0d, 1.0d, 1.0d }, transformer.transformAttribute(new MultiValueAttributeValue(type, Arrays.asList("b", "c"))), DELTA);
		assertArrayEquals(new double[] { 1.0d, 1.0d, 1.0d }, transformer.transformAttribute(new MultiValueAttributeValue(type, Arrays.asList("a", "b", "c"))), DELTA);
	}

}
