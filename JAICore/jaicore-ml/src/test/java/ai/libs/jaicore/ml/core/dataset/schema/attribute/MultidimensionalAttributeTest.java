package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MultidimensionalAttributeTest /* extends AAttributeTest possibly? */ {

	private final static String TEST_ATTR_NAME = "testattribute";
	private final static String TEST_SERIALISE_STRING = "[[1.0 2.0] [3.0 4.0] [5.0 6.0]]";
	private final static int Test_ATTR_XSIZE = 3;
	private final static int Test_ATTR_YSIZE = 2;
	static MultidimensionalAttribute testAttr;

	@BeforeAll
	public static void setup() {
		testAttr = new MultidimensionalAttribute(TEST_ATTR_NAME, Test_ATTR_XSIZE, Test_ATTR_YSIZE);
	}

	@Test
	public void checkSetup() {
		assertTrue("parameters do not match", testAttr.getXsize() == Test_ATTR_XSIZE && testAttr.getYsize() == Test_ATTR_YSIZE && testAttr.getName().equals(TEST_ATTR_NAME));
	}

	@Test
	public void serializeAttributeValue() {
		MultidimensionalAttributeValue testValue = testAttr.deserializeAttributeValue(TEST_SERIALISE_STRING);
		String compString = testAttr.serializeAttributeValue(testValue);
		System.out.println(compString);
		assertTrue("serialisation deserialisation mistake", compString.equals(TEST_SERIALISE_STRING));

	}

}
