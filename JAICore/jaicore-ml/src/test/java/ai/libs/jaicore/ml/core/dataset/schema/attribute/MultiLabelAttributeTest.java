package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IMultiLabelAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IMultiLabelAttributeValue;
import org.junit.BeforeClass;
import org.junit.Test;

import ai.libs.jaicore.basic.sets.SetUtil;

public class MultiLabelAttributeTest extends AAttributeTest {

	private static final String NAME = "myAtt";
	private static final List<String> VALUES = Arrays.asList("A", "B", "C");
	private static IMultiLabelAttribute att;

	private static final Collection<String> SAMPLE_VALID_ATT_VALUE = Arrays.asList("A", "C");
	private static final Collection<String> SAMPLE_INVALID_ATT_VALUE = Arrays.asList("A", "D");

	private static final String EXPECTED_STRING_DESCRIPTION = "MultiLabelAttribute " + NAME + " " + VALUES.toString();
	private static final String EXPECTED_ATT_SERIALIZATION_STRING = SetUtil.implode(SAMPLE_VALID_ATT_VALUE, MultiLabelAttribute.MULTI_LABEL_VALUE_SEP);

	@BeforeClass
	public static void setup() {
		att = new MultiLabelAttribute(NAME, VALUES);
	}

	public String getStringDescriptionOfDomain() {
		return EXPECTED_STRING_DESCRIPTION;
	}

	@Override
	public String getExpectedAttributeName() {
		return NAME;
	}

	@Override
	public Collection<?> getValuesThatMustBeContained() {
		return VALUES;
	}

	@Override
	public IAttribute getTestedAttribute() {
		return new MultiLabelAttribute(NAME, VALUES);
	}

	@Override
	public Object getExampleObject() {
		return SAMPLE_VALID_ATT_VALUE;
	}

	@Override
	public IAttributeValue getExampleAttributeValue() {
		return new MultiLabelAttributeValue(new MultiLabelAttribute(NAME, VALUES), SAMPLE_VALID_ATT_VALUE);
	}

	@Override
	public String getExampleSerialization() {
		return EXPECTED_ATT_SERIALIZATION_STRING;
	}

	@Test
	public void testGetStringDescriptionOfDomain() {
		assertEquals("The string description of the domain does not match", EXPECTED_STRING_DESCRIPTION, att.getStringDescriptionOfDomain());
	}

	@Test
	public void testGetAsAttributeValue() {
		IMultiLabelAttributeValue val = att.getAsAttributeValue(SAMPLE_VALID_ATT_VALUE);
		assertTrue("Does not match the expected size", val.getValue().size() == SAMPLE_VALID_ATT_VALUE.size());
		assertTrue("Labels differ in attribute value.", val.getValue().containsAll(SAMPLE_VALID_ATT_VALUE));

		this.throwsException(() -> att.getAsAttributeValue(SAMPLE_INVALID_ATT_VALUE), IllegalArgumentException.class);
		this.throwsException(() -> att.getAsAttributeValue(-1), IllegalArgumentException.class);
	}

	@Test
	public void testSerialization() {
		assertEquals(EXPECTED_ATT_SERIALIZATION_STRING, att.serializeAttributeValue(SAMPLE_VALID_ATT_VALUE));
		assertEquals(EXPECTED_ATT_SERIALIZATION_STRING, att.serializeAttributeValue(att.getAsAttributeValue(SAMPLE_VALID_ATT_VALUE)));
	}

	@Override
	@Test
	public void testDeserialization() {
		IMultiLabelAttributeValue val = att.getAsAttributeValue(att.deserializeAttributeValue(EXPECTED_ATT_SERIALIZATION_STRING));
		assertEquals(SAMPLE_VALID_ATT_VALUE.size(), val.getValue().size());
		assertTrue(val.getValue().containsAll(SAMPLE_VALID_ATT_VALUE));
	}

	@Test
	public void testIsValidValue() {
		assertTrue(att.isValidValue(SAMPLE_VALID_ATT_VALUE));
		assertTrue(att.isValidValue(att.getAsAttributeValue(SAMPLE_VALID_ATT_VALUE)));

		att.isValidValue(SAMPLE_INVALID_ATT_VALUE);

		assertFalse(att.isValidValue(SAMPLE_INVALID_ATT_VALUE));
		assertFalse(att.isValidValue("X"));
		assertFalse(att.isValidValue(1));
	}

	public void throwsException(final Runnable run, final Class<?> exception) {
		boolean exceptionFlag = false;
		try {
			run.run();
		} catch (Exception e) {
			if (exception.isAssignableFrom(exception)) {
				exceptionFlag = true;
			}
		}
		assertTrue("Expected the occurence of an exception " + exception.getName() + " but wwas not thrown.", exceptionFlag);
	}

}
