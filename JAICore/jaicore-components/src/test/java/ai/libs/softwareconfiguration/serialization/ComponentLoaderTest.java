package ai.libs.softwareconfiguration.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import ai.libs.jaicore.components.model.BooleanParameterDomain;
import ai.libs.jaicore.components.model.CategoricalParameterDomain;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.model.Parameter;
import ai.libs.jaicore.components.model.ParameterRefinementConfiguration;
import ai.libs.jaicore.components.serialization.ComponentLoader;

@SuppressWarnings("SimplifiableJUnitAssertion")
public class ComponentLoaderTest {

	@Test
	public void testLoadFromFile() throws IOException {
		ComponentLoader loader = new ComponentLoader(new File("testrsc/difficultproblem.json"));
		List<Component> components = loader.getComponents().stream().sorted((c1,c2) -> c1.getName().compareTo(c2.getName())).collect(Collectors.toList());

		/* check number of components */
		assertEquals(2, components.size());
		Component c1 = components.get(0);

		/* check parameter names of first component */
		assertEquals(5, c1.getParameters().size());
		List<Parameter> params = c1.getParameters().stream().sorted((p1,p2) -> p1.getName().compareTo(p2.getName())).collect(Collectors.toList());
		for (int i = 1; i <= 5; i++) {
			Parameter p =  params.get(i - 1);
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
		Map<Component, Map<Parameter, ParameterRefinementConfiguration>> parameterConfig = loader.getParamConfigs();
		for (Component c : loader.getComponents()) {
			assertNotNull(parameterConfig.get(c));
		}
		assertNotNull(parameterConfig.get(c1).get(params.get(2)));
		assertNotNull(parameterConfig.get(c1).get(params.get(3)));
		assertNotNull(parameterConfig.get(c1).get(params.get(4)));
	}

	@Test
	public void testEqualityOfTwoLoadingProceduresOnDifficultProblem() throws IOException {
		this.testEqualityOfTwoLoadingProcedures("testrsc/difficultproblem.json");
	}

	@Test
	public void testEqualityOfTwoLoadingProceduresOnAutoWekaSearchSpace() throws IOException {
		this.testEqualityOfTwoLoadingProcedures("testrsc/weka/weka-all-autoweka.json");
	}

	public void testEqualityOfTwoLoadingProcedures(final String filename) throws IOException {
		ComponentLoader loader1 = new ComponentLoader(new File(filename));
		ComponentLoader loader2 = new ComponentLoader(new File(filename));
		List<Component> components1 = new ArrayList<>(loader1.getComponents());
		List<Component> components2 = new ArrayList<>(loader2.getComponents());
		int n = components1.size();
		for (int i = 0; i < n; i++) {
			Component c1 = components1.get(i);
			Component c2 = components2.get(i);

			/* check equality of names*/
			assertEquals(c1.getName(), c2.getName());

			/* check equality of parameters */
			List<Parameter> parameters1 = new ArrayList<>(c1.getParameters());
			List<Parameter> parameters2 = new ArrayList<>(c2.getParameters());
			int m = parameters1.size();
			assertEquals(m, parameters2.size());
			for (int j = 0; j < m; j++) {
				Parameter p1 = parameters1.get(j);
				Parameter p2 = parameters2.get(j);
				assertEquals(p1.getName(), p2.getName());
				assertEquals(p1.getDefaultValue(), p2.getDefaultValue());
				assertEquals(p1.getDefaultDomain(), p2.getDefaultDomain());
				assertEquals(p1, p2);
			}

			/* check equality of interfaces */
			assertEquals(c1.getRequiredInterfaces(), c2.getRequiredInterfaces());
			assertEquals(c1.getProvidedInterfaces(), c2.getProvidedInterfaces());

			/* check equality of dependencies */
			assertEquals(c1.getDependencies(), c2.getDependencies());

			/* check overall equality of components */
			assertEquals(c1, c2);
		}
		assertEquals(loader1.getComponents(), loader2.getComponents());
		assertEquals(loader1.getParamConfigs(), loader2.getParamConfigs());
	}

	@Test
	public void testLoadFromResource() throws IOException {
		Collection<Component> components = new ComponentLoader(new File("testrsc/weka-all-autoweka.json")).getComponents();
		assertTrue(!components.isEmpty());
	}

}