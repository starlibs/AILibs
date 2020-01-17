package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class MultiLabelAttributeValueTest {

	private static final List<String> ATT_DOM = Arrays.asList("A", "B", "C");
	private static final List<String> ATT_VALUES = Arrays.asList("A,", "C");

	private static final MultiLabelAttribute att = new MultiLabelAttribute("myAtt", ATT_DOM);
	private static MultiLabelAttributeValue attVal;

	@BeforeClass
	public static void setup() {
		attVal = new MultiLabelAttributeValue(att, ATT_VALUES);
	}

	@Test
	public void testValues() {
		assertEquals(ATT_VALUES.size(), attVal.getValue().size());
		assertTrue(ATT_VALUES.containsAll(attVal.getValue()));
	}

	@Test
	public void testAttribute() {
		assertEquals(att, attVal.getAttribute());
	}

}
