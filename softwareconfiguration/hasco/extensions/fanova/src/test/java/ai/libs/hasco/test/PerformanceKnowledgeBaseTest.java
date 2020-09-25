package ai.libs.hasco.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import ai.libs.hasco.knowledgebase.PerformanceKnowledgeBase;
import ai.libs.jaicore.basic.Tester;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.CategoricalParameterDomain;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.model.Parameter;

public class PerformanceKnowledgeBaseTest extends Tester {

	@Test
	public void test() {
		PerformanceKnowledgeBase pKB = new PerformanceKnowledgeBase();
		Component comp = new Component("TestComponent");
		NumericParameterDomain domain1 = new NumericParameterDomain(false, 0.0, 5.0);
		Parameter param1 = new Parameter("Param1", domain1, 2.0);
		ArrayList<String> values = new ArrayList<String>();
		values.add("Val1");
		values.add("Val2");
		values.add("Val3");
		CategoricalParameterDomain domain2 = new CategoricalParameterDomain(values);
		Parameter param2 = new Parameter("Param2", domain2, "Val2");
		NumericParameterDomain domain3 = new NumericParameterDomain(true, 7, 19);
		Parameter param3 = new Parameter("Param3", domain3, 7);
		comp.addParameter(param1);
		comp.addParameter(param2);
		comp.addParameter(param3);

		HashMap<String, String> paramValues1 = new HashMap<String, String>();
		paramValues1.put("Param1", "4.0");
		paramValues1.put("Param2", "Val1");
		paramValues1.put("Param3", "14");

		HashMap<String, String> paramValues2 = new HashMap<String, String>();
		paramValues2.put("Param1", "1.0");
		paramValues2.put("Param2", "Val3");
		paramValues2.put("Param3", "11");

		HashMap<String, String> paramValues3 = new HashMap<String, String>();
		paramValues3.put("Param1", "2.0");
		paramValues3.put("Param2", "Val2");
		paramValues3.put("Param3", "13");

		HashMap<String, List<IComponentInstance>> satisfactionOfRequiredInterfaces = new HashMap<>();

		ComponentInstance ci1 = new ComponentInstance(comp, paramValues1, satisfactionOfRequiredInterfaces);
		ComponentInstance ci2 = new ComponentInstance(comp, paramValues2, satisfactionOfRequiredInterfaces);
		ComponentInstance ci3 = new ComponentInstance(comp, paramValues3, satisfactionOfRequiredInterfaces);

		pKB.addPerformanceSample("test", ci1, 0.7754, false);
		pKB.addPerformanceSample("test", ci2, 0.1154, false);
		pKB.addPerformanceSample("test", ci3, 0.3333, false);

		LOGGER.info("PKB has k completely distinct distinct samples: {}", pKB.kCompletelyDistinctSamplesAvailable("test", ci1, 2));
		LOGGER.info("PKB has k distinct values: {}", pKB.kDistinctAttributeValuesAvailable("test", ci1, 4));
		LOGGER.info("{}", pKB.getPerformanceSamplesForIndividualComponent("test", comp));

		assertTrue(true);
	}

}
