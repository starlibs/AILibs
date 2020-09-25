package ai.libs.softwareconfiguration.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IComponentInstanceConstraint;
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.api.INumericParameterRefinementConfigurationMap;
import ai.libs.jaicore.components.api.IParameter;
import ai.libs.jaicore.components.model.BooleanParameterDomain;
import ai.libs.jaicore.components.model.CategoricalParameterDomain;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.logging.LoggerUtil;

@SuppressWarnings("SimplifiableJUnitAssertion")
public class ComponentLoaderTest {

	@Test
	public void testLoadFromFile() throws IOException {
		IComponentRepository repo = new ComponentSerialization().deserializeRepository(new File("testrsc/difficultproblem.json"));
		List<IComponent> components = repo.stream().sorted((c1,c2) -> c1.getName().compareTo(c2.getName())).collect(Collectors.toList());

		/* check number of components */
		assertEquals(2, components.size());
		IComponent c1 = components.get(0);

		/* check parameter names of first component */
		assertEquals(5, c1.getParameters().size());
		List<IParameter> params = c1.getParameters().stream().sorted((p1,p2) -> p1.getName().compareTo(p2.getName())).collect(Collectors.toList());
		for (int i = 1; i <= 5; i++) {
			IParameter p =  params.get(i - 1);
			assertEquals("a" + i, p.getName());
			switch (i) {

			case 1:
				assertTrue(p.getDefaultDomain() instanceof BooleanParameterDomain);
				break;
			case 2:
				assertTrue(p.getDefaultDomain() instanceof CategoricalParameterDomain);
				break;
			default:
				assertTrue(p.getDefaultDomain() instanceof NumericParameterDomain);
				break;
			}
		}

		/* test parameters */
		INumericParameterRefinementConfigurationMap parameterConfig = new ComponentSerialization().deserializeParamMap(new File("testrsc/difficultproblem.json"));
		for (IComponent c : components) {
			assertNotNull(parameterConfig.getParameterNamesForWhichARefinementIsDefined(c));
		}
		assertNotNull(parameterConfig.getRefinement(c1, params.get(2)));
		assertNotNull(parameterConfig.getRefinement(c1, params.get(3)));
		assertNotNull(parameterConfig.getRefinement(c1, params.get(4)));
	}

	@Test
	public void testEqualityOfTwoLoadingProceduresOnDifficultProblem() throws IOException {
		this.testEqualityOfTwoLoadingProcedures("testrsc/difficultproblem.json");
	}

	@Test
	public void testEqualityOfTwoLoadingProceduresOnAutoWekaSearchSpace() throws IOException {
		this.testEqualityOfTwoLoadingProcedures("testrsc/weka/weka-all-autoweka.json");
	}

	@Test
	public void testVariableReplacement() throws IOException {
		Map<String, String> replacement = new HashMap<>();
		String compNameA = "CompA";
		String compNameB = "CompB";
		replacement.put("nameOfA", compNameA);
		replacement.put("typeOfa", "boolean");
		replacement.put("defaultOfa", "true");
		replacement.put("nameOfParama", "a");
		replacement.put("nameOfB", compNameB);
		replacement.put("domainOfb", "[\"v1\", \"v2\", \"v3\", \"v4\", \"v5\"]");
		replacement.put("defaultOfb", "v4");
		replacement.put("nameOfParamb", "b");
		IComponentRepository repo = new ComponentSerialization(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM).deserializeRepository(new File("testrsc/problemwithvars/main.json"), replacement);
		IComponent compA = repo.getComponent(compNameA);
		IParameter param = compA.getParameter("a");
		assertTrue(param.getDefaultDomain() instanceof BooleanParameterDomain);
		assertEquals(true, param.getDefaultValue());


		IComponent compB = repo.getComponent(compNameB);
		IParameter paramb = compB.getParameter("b");
		assertTrue(paramb.getDefaultDomain() instanceof CategoricalParameterDomain);
		assertEquals("v4", paramb.getDefaultValue());
	}

	public void testEqualityOfTwoLoadingProcedures(final String filename) throws IOException {
		List<IComponent> components1 = new ArrayList<>(new ComponentSerialization().deserializeRepository(new File(filename)));
		List<IComponent> components2 = new ArrayList<>(new ComponentSerialization().deserializeRepository(new File(filename)));
		int n = components1.size();
		for (int i = 0; i < n; i++) {
			IComponent c1 = components1.get(i);
			IComponent c2 = components2.get(i);

			/* check equality of names*/
			assertEquals(c1.getName(), c2.getName());

			/* check equality of parameters */
			List<IParameter> parameters1 = new ArrayList<>(c1.getParameters());
			List<IParameter> parameters2 = new ArrayList<>(c2.getParameters());
			int m = parameters1.size();
			assertEquals(m, parameters2.size());
			for (int j = 0; j < m; j++) {
				IParameter p1 = parameters1.get(j);
				IParameter p2 = parameters2.get(j);
				assertEquals(p1.getName(), p2.getName());
				assertEquals(p1.getDefaultValue(), p2.getDefaultValue());
				assertEquals(p1.getDefaultDomain(), p2.getDefaultDomain());
				assertEquals(p1, p2);
			}

			/* check equality of interfaces */
			assertEquals(c1.getRequiredInterfaces(), c2.getRequiredInterfaces());
			assertEquals(c1.getProvidedInterfaces(), c2.getProvidedInterfaces());

			/* check equality of dependencies */
			assertEquals(c1.getParameterDependencies(), c2.getParameterDependencies());

			/* check overall equality of components */
			assertEquals(c1, c2);
		}
		assertEquals(components1, components2);
		assertEquals(new ComponentSerialization().deserializeParamMap(new File(filename)), new ComponentSerialization().deserializeParamMap(new File(filename)));
	}

	@Test
	public void testConstraints() throws IOException {
		IComponentRepository repo = new ComponentSerialization(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM).deserializeRepository(new File("testrsc/tinyproblemwithconstraints.json"));
		Collection<IComponentInstanceConstraint> constraintsAsSet = repo.getConstraints();
		assertFalse(constraintsAsSet.isEmpty());
		assertEquals(2, constraintsAsSet.size());
		List<IComponentInstanceConstraint> constraints = new ArrayList<>(constraintsAsSet);

		/* check first (negative) constraint */
		IComponentInstanceConstraint c1 = constraints.get(0);
		assertFalse(c1.isPositive());
		IComponentInstance premisePattern1 = c1.getPremisePattern();
		IComponentInstance conclusionPattern1 = c1.getConclusionPattern();
		assertEquals("COMPA", premisePattern1.getComponent().getName());
		assertEquals("COMPA", conclusionPattern1.getComponent().getName());
		assertTrue(premisePattern1.getParameterValues().isEmpty());
		assertTrue(conclusionPattern1.getParameterValues().isEmpty());
		assertEquals("COMPB", premisePattern1.getSatisfactionOfRequiredInterface("rif1").iterator().next().getComponent().getName());
		assertEquals("COMPC", conclusionPattern1.getSatisfactionOfRequiredInterface("rif2").iterator().next().getComponent().getName());

		/* check first (positive) constraint */
		IComponentInstanceConstraint c2 = constraints.get(1);
		assertTrue(c2.isPositive());
		IComponentInstance premisePattern2 = c2.getPremisePattern();
		IComponentInstance conclusionPattern2 = c2.getConclusionPattern();
		assertEquals("COMPA", premisePattern2.getComponent().getName());
		assertEquals("COMPA", conclusionPattern2.getComponent().getName());
		assertTrue(premisePattern2.getParameterValues().isEmpty());
		assertTrue(conclusionPattern2.getParameterValues().isEmpty());
		assertEquals("COMPD", premisePattern2.getSatisfactionOfRequiredInterface("rif2").iterator().next().getComponent().getName());
		assertEquals("COMPB", conclusionPattern2.getSatisfactionOfRequiredInterface("rif1").iterator().next().getComponent().getName());
	}
}