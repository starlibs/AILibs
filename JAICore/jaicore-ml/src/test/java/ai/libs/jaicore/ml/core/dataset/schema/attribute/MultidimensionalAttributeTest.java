package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.ArrayList;
import java.util.Collection;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.junit.jupiter.api.BeforeAll;

public class MultidimensionalAttributeTest extends AAttributeTest {

	private final static String TEST_ATTR_NAME = "testattribute";
	private final static double[][] TEST_CONTAINED_VAL = { { 1.0, 2.0 }, { 3.0, 4.0 }, { 5.0, 6.0 } };
	private final static int Test_ATTR_XSIZE = 3;
	private final static int Test_ATTR_YSIZE = 2;

	static MultidimensionalAttribute testAttr;

	@BeforeAll
	public static void SetupApi() {
		testAttr = new MultidimensionalAttribute(TEST_ATTR_NAME, Test_ATTR_XSIZE, Test_ATTR_YSIZE);
	}

	@Override
	public String getExpectedAttributeName() {
		return TEST_ATTR_NAME;
	}

	@Override
	public Collection<?> getValuesThatMustBeContained() {
		Collection<double[][]> testList = new ArrayList<double[][]>();
		testList.add(TEST_CONTAINED_VAL);
		return testList;
	}

	@Override
	public IAttribute getTestedAttribute() {
		return testAttr;
	}

}
