package ai.libs.hasco.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ai.libs.hasco.core.HASCOUtil;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IParameter;
import ai.libs.jaicore.components.api.IParameterDomain;
import ai.libs.jaicore.components.model.CategoricalParameterDomain;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.logic.fol.structure.ConstantParam;
import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.Monom;

public class HASCOUtilTester {

	private static final String pathToFiles = "../../../JAICore/jaicore-components/";

	@Test
	public void testComponentInstanceBuilder() throws Exception {
		File compfile = new File(pathToFiles + "testrsc/simpleproblemwithtwocomponentsandnumericparam.json");
		Monom state = new Monom(
				"resolves(request, A, A, solution) & parameterContainer(A, a, solution, newVar2) & val(newVar3, v2) & closed(newVar3) & parameterContainer(B, b, newVar5, newVar6) & parameterContainer(B, a, newVar5, newVar7) & parameterContainer(A, b, solution, newVar3) & parameterFocus(solution, a, NaN) & val(newVar6, v3) & val(newVar7, true) & interfaceMember(newVar4, newVar1, 1) & component(solution) & component(request) & overwritten(newVar7) & overwritten(newVar6) & component(newVar5) & resolves(newVar4, IFA, B, newVar5) & overwritten(newVar3) & interfaceGroup(A, i1, solution, newVar1) & overwritten(newVar2)");
		state.add(new Literal("val", Arrays.asList(new ConstantParam("newVar2"), new ConstantParam("[12.5, 13.5]")))); // add this separately, because the comma in the interval cannot be handled by the monom constructor
		ComponentSerialization serializer = new ComponentSerialization();

		/* test that parameters have the correct interval value */
		ComponentInstance solution = HASCOUtil.getComponentInstanceFromState(serializer.deserializeRepository(compfile), state, "solution", false);
		assertNotNull(solution);
		assertEquals("A", solution.getComponent().getName());
		assertEquals("[12.5, 13.5]", solution.getParameterValue("a"));
		assertEquals("v2", solution.getParameterValue("b"));
		IComponentInstance ciSub = solution.getSatisfactionOfRequiredInterface("i1").iterator().next();
		assertNotNull(ciSub);
		assertEquals("B", ciSub.getComponent().getName());
		assertEquals("true", ciSub.getParameterValue("a"));
		assertEquals("v3", ciSub.getParameterValue("b"));

		/* now test that the value is correctly resolved (middle of the interval) */
		solution = HASCOUtil.getComponentInstanceFromState(serializer.deserializeRepository(compfile), state, "solution", true);
		assertNotNull(solution);
		assertEquals("A", solution.getComponent().getName());
		assertEquals(13.0, Double.parseDouble(solution.getParameterValue("a")), 0.0);
		assertEquals("v2", solution.getParameterValue("b"));
		ciSub = solution.getSatisfactionOfRequiredInterface("i1").iterator().next();
		assertNotNull(ciSub);
		assertEquals("B", ciSub.getComponent().getName());
		assertEquals("true", ciSub.getParameterValue("a"));
		assertEquals("v3", ciSub.getParameterValue("b"));
	}

	@Test
	public void testParameterDomainUpdates() throws Exception {
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(new File(pathToFiles + "testrsc/problemwithdependencies.json"), "IFace", n -> 0.0);
		IComponent bComponent = problem.getComponents().stream().filter(c -> c.getName().equals("B")).findFirst().get();
		IParameter dParameter = bComponent.getParameter("d");

		/* first check that the domain is default if c is not set at all */
		{
			ComponentInstance inst = new ComponentInstance(bComponent, null, null);
			Map<IParameter, IParameterDomain> newDomains = HASCOUtil.getUpdatedDomainsOfComponentParameters(inst);
			assertEquals(dParameter.getDefaultDomain(), newDomains.get(dParameter));
		}

		/* now check that the domain remains default if c is explicitly set to false */
		{
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("c", "false");
			ComponentInstance inst = new ComponentInstance(bComponent, parameterValues, null);
			Map<IParameter, IParameterDomain> newDomains = HASCOUtil.getUpdatedDomainsOfComponentParameters(inst);
			assertEquals(dParameter.getDefaultDomain(), newDomains.get(dParameter));
		}

		/* now check that the domain is changed in the intended manner when c is true */
		{
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("c", "true");
			ComponentInstance inst = new ComponentInstance(bComponent, parameterValues, null);
			Map<IParameter, IParameterDomain> newDomains = HASCOUtil.getUpdatedDomainsOfComponentParameters(inst);
			Set<String> expectedValues = new HashSet<>();
			expectedValues.add("blue");
			expectedValues.add("white");
			expectedValues.add("red");
			expectedValues.add("green");
			expectedValues.add("black");
			IParameterDomain expectedDomain = new CategoricalParameterDomain(expectedValues);
			assertEquals(expectedDomain, newDomains.get(dParameter));
		}
	}
}
