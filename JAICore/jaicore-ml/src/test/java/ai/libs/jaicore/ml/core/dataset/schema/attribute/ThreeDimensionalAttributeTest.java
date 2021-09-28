package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.ArrayList;
import java.util.Collection;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.junit.jupiter.api.BeforeAll;

public class ThreeDimensionalAttributeTest extends AAttributeTest {

	private final static String TEST_ATTR_NAME = "testattribute";
	private final static double[][][] TEST_CONTAINED_VAL = { { { 1.0, 2.0 }, { 3.0, 4.0 }, { 5.0, 6.0 } }, { { 1.1, 2.1 }, { 3.1, 4.1 }, { 5.1, 6.1 } }, { { 1.2, 2.2 }, { 3.2, 4.2 }, { 5.2, 6.2 } },
			{ { 1.3, 2.3 }, { 3.3, 4.3 }, { 5.3, 6.3 } } };
	private final static int TEST_ATTR_XSIZE = 4;
	private final static int TEST_ATTR_YSIZE = 3;
	private final static int TEST_ATTR_ZSIZE = 2;

	private static ThreeDimensionalAttribute testattr;

	@BeforeAll
	public static void SetupApi() {
		testattr = new ThreeDimensionalAttribute(TEST_ATTR_NAME, TEST_ATTR_XSIZE, TEST_ATTR_YSIZE, TEST_ATTR_ZSIZE);
	}

	@Override
	public String getExpectedAttributeName() {
		return TEST_ATTR_NAME;
	}

	@Override
	public Collection<double[][][]> getValuesThatMustBeContained() {
		Collection<double[][][]> returnvalue = new ArrayList();
		returnvalue.add(TEST_CONTAINED_VAL);
		return returnvalue;

	}

	@Override
	public IAttribute getTestedAttribute() {
		return testattr;
	}

}
