package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IMultiLabelAttribute;
import org.junit.BeforeClass;
import org.junit.Test;

import ai.libs.jaicore.ml.core.dataset.schema.attribute.MultiLabelAttribute;

public class MultiLabelAttributeTest {

	private static final String NAME = "myAtt";
	private static final List<String> VALUES = Arrays.asList("A", "B", "C");
	private static IMultiLabelAttribute att;

	@BeforeClass
	public static void setup() {
		att = new MultiLabelAttribute(NAME, VALUES);
	}

	@Test
	public void testGetName() {
		assertEquals("Name is not correctly stored in MultiLabelAttribute", NAME, att.getName());
	}

	@Test
	public void testGetValues() {
		assertEquals("The number of values is not correct", VALUES.size(), att.getValues().size());
		assertTrue("The set of possible values is not correct", VALUES.containsAll(att.getValues()));
	}

}
