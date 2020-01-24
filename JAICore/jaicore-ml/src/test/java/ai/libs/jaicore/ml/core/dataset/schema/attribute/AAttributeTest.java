package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.junit.Test;

public abstract class AAttributeTest {

	public abstract String getExpectedAttributeName();

	public abstract Collection<?> getValuesThatMustBeContained();

	public abstract IAttribute getTestedAttribute();

	@Test
	public void testNameOfAttribute() {
		assertEquals("The returned name of the attribute does not match the defined one.", this.getExpectedAttributeName(), this.getTestedAttribute().getName());
	}

	@Test
	public void testThatValuesAreContained() {
		IAttribute att = this.getTestedAttribute();
		for (Object val : this.getValuesThatMustBeContained()) {
			assertTrue("The internal values in the attribute do not match the defined ones.", att.isValidValue(val));
		}
	}

	@Test
	public void testThatValuesAreContainedAfterSerializationDeserialization() {
		IAttribute att = this.getTestedAttribute();
		for (Object val : this.getValuesThatMustBeContained()) {
			Object reserializedValue = att.deserializeAttributeValue(att.serializeAttributeValue(val));
			assertTrue("The value \"" + val + "\" is reserialized to \"" + reserializedValue + "\", which is not a valid value for the attribute.", att.isValidValue(reserializedValue));
		}
	}
}
