package ai.libs.hyperopt.optimizer.pcs.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import ai.libs.hasco.model.CategoricalParameterDomain;
import ai.libs.hasco.model.NumericParameterDomain;
import ai.libs.hasco.model.Parameter;
import ai.libs.hasco.serialization.ComponentLoader;
import ai.libs.jaicore.basic.sets.SetUtil;

public class ComponentToPCSConverterUtilTest {

	final String testComponentA = "A";

	final String numericParamName = "p1";
	final Parameter testNumericDoubleParam = new Parameter(this.numericParamName, new NumericParameterDomain(false, 0.0, 1.0), 0.5);
	final Parameter testNumericIntParam = new Parameter(this.numericParamName, new NumericParameterDomain(true, 0.0, 12.0), 1.0);
	final Parameter testNumericIgnoreParam = new Parameter(this.numericParamName, new NumericParameterDomain(true, 0.0, 0.0), 1.0);

	final Parameter testCategoricalParam = new Parameter(this.numericParamName, new CategoricalParameterDomain(Arrays.asList("X", "Y", "Z")), "Y");

	private ComponentToPCSConverterUtil converter = new ComponentToPCSConverterUtil(null, null);

	@Test
	public void testConvertNumericParameter() {
		String expectedDouble = SetUtil.implode(Arrays.asList(this.testComponentA, this.numericParamName), ".") + " [0.0,1.0][0.5]";
		assertEquals("Numeric parameter format does not match.", expectedDouble, this.converter.convertNumericParameter(this.testComponentA, this.testNumericDoubleParam));

		String expectedInt = SetUtil.implode(Arrays.asList(this.testComponentA, this.numericParamName), ".") + " [0,12][1]i";
		assertEquals("Numeric parameter format does not match.", expectedInt, this.converter.convertNumericParameter(this.testComponentA, this.testNumericIntParam));

		// This will also trigger a warn log output.
		assertNull("Numeric parameter needs to be ignored.", this.converter.convertNumericParameter(this.testComponentA, this.testNumericIgnoreParam));
	}

	@Test
	public void testConvertCategoricalParameter() {
		String expected = SetUtil.implode(Arrays.asList(this.testComponentA, this.numericParamName), ".") + " {X,Y,Z}[Y]";
		assertEquals("Numeric parameter format does not match.", expected, this.converter.convertCategoricalParameter(this.testComponentA, this.testCategoricalParam));
	}

	@Test
	public void testSimpleProblem() throws IOException {
		ComponentLoader cl = new ComponentLoader(new File("testrsc/weka/weka-all-autoweka.json"));
		ComponentToPCSConverterUtil converter = new ComponentToPCSConverterUtil(cl.getComponents(), "weka.classifiers.functions.SimpleLogistic");
		System.out.println(converter.toPCS());
	}

}
