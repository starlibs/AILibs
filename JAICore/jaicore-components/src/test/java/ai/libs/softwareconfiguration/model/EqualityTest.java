package ai.libs.softwareconfiguration.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import ai.libs.jaicore.components.model.CategoricalParameterDomain;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.model.Parameter;

public class EqualityTest {

	@Test
	public void testIntegerParameterEquality() {
		Parameter p1 = new Parameter("a", new NumericParameterDomain(false, 0, 1), 0);
		Parameter p2 = new Parameter("a", new NumericParameterDomain(false, 0, 1), 0);
		assertEquals(p1, p2);
	}

	@Test
	public void testNumericParameterEquality() {
		Parameter p1 = new Parameter("a", new NumericParameterDomain(true, 0, 1), 0);
		Parameter p2 = new Parameter("a", new NumericParameterDomain(true, 0, 1), 0);
		assertEquals(p1, p2);
	}

	@Test
	public void testComponentEquality() {
		Component c1 = new Component("a");
		Component c2 = new Component("a");
		assertEquals(c1, c2);
	}

	@Test
	public void testIntegerParameterDomainEquality() {
		NumericParameterDomain nd1 = new NumericParameterDomain(true, 0, 1);
		NumericParameterDomain nd2 = new NumericParameterDomain(true, 0, 1);
		assertEquals(nd1, nd2);
	}

	@Test
	public void testNumericParameterDomainEquality() {
		NumericParameterDomain nd1 = new NumericParameterDomain(false, 0, 1);
		NumericParameterDomain nd2 = new NumericParameterDomain(false, 0, 1);
		assertEquals(nd1, nd2);
	}

	@Test
	public void testCategoricalParameterDomainEquality() {
		CategoricalParameterDomain nd1 = new CategoricalParameterDomain(Arrays.asList("a", "b", "c"));
		CategoricalParameterDomain nd2 = new CategoricalParameterDomain(Arrays.asList("a", "b", "c"));
		assertEquals(nd1, nd2);
	}
}
