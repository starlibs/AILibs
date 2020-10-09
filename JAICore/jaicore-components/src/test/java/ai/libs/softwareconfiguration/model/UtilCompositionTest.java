package ai.libs.softwareconfiguration.model;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.ComponentUtil;
import ai.libs.jaicore.components.model.CompositionProblemUtil;
import ai.libs.jaicore.components.serialization.ComponentSerialization;

public class UtilCompositionTest {

	@Test
	public void testNumberOfComponents() {
		Component component1 = new Component("Component1");
		Component component2 = new Component("Component2");
		Component component3 = new Component("Component3");
		Component component4 = new Component("Component4");
		List<Component> groundTruth = new LinkedList<Component>();
		groundTruth.add(component1);
		groundTruth.add(component4);
		groundTruth.add(component2);
		groundTruth.add(component3);
		component1.addRequiredInterface("Interface1", "Interface1");
		component1.addRequiredInterface("Interface2", "Interface2");
		component2.addRequiredInterface("Interface1", "Interface1");
		Map<String, List<IComponentInstance>> sat1 = new HashMap<>();
		Map<String, List<IComponentInstance>> sat2 = new HashMap<>();
		Map<String, List<IComponentInstance>> sat3 = new HashMap<>();
		Map<String, List<IComponentInstance>> sat4 = new HashMap<>();
		Map<String, String> parameterValues = new HashMap<>();
		ComponentInstance instance4 = new ComponentInstance(component4, parameterValues, sat4);
		ComponentInstance instance3 = new ComponentInstance(component3, parameterValues, sat3);
		sat2.put("Interface1", Arrays.asList(instance3));
		ComponentInstance instance2 = new ComponentInstance(component2, parameterValues, sat2);
		sat1.put("Interface1", Arrays.asList(instance2));
		sat1.put("Interface2", Arrays.asList(instance4));
		ComponentInstance instance1 = new ComponentInstance(component1, parameterValues, sat1);
		List<IComponent> components = CompositionProblemUtil.getComponentsOfComposition(instance1);
		for (int i = 0; i < groundTruth.size(); i++) {
			assertEquals(components.get(i), groundTruth.get(i));
		}
	}

	@Test
	public void testNumberOfUnparametrizedCompositions() throws IOException {
		IComponentRepository repo = new ComponentSerialization().deserializeRepository(new File("./testrsc/simplerecursiveproblem.json"));
		assertEquals(4, ComponentUtil.getNumberOfUnparametrizedCompositions(repo, "IFace"));
	}
}
