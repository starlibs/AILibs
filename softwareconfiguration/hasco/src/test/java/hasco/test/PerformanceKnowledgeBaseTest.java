package hasco.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import hasco.knowledgebase.PerformanceKnowledgeBase;
import hasco.model.CategoricalParameterDomain;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;

public class PerformanceKnowledgeBaseTest {

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

		HashMap<String, ComponentInstance> satisfactionOfRequiredInterfaces = new HashMap<String, ComponentInstance>();

		ComponentInstance ci1 = new ComponentInstance(comp, paramValues1, satisfactionOfRequiredInterfaces);
		ComponentInstance ci2 = new ComponentInstance(comp, paramValues2, satisfactionOfRequiredInterfaces);
		ComponentInstance ci3 = new ComponentInstance(comp, paramValues3, satisfactionOfRequiredInterfaces);

		System.out.println(ci1.getParameterValues());

		pKB.addPerformanceSample("test", ci1, 0.7754, false);
		pKB.addPerformanceSample("test", ci2, 0.1154, false);
		pKB.addPerformanceSample("test", ci3, 0.3333, false);

		// System.out.println(pKB.createInstancesForPerformanceSamples("test", ci1));
		// String identifier = Util.getComponentNamesOfComposition(ci1);
		// System.out.println("Number completely distinct samples: " + pKB.getNumCompletelyDistinctSamples("test", identifier));

		System.out.println("PKB has k completely distinct distinct samples: " + pKB.kCompletelyDistinctSamplesAvailable("test", ci1, 2));
		System.out.println("PKB has k distinct values: " + pKB.kDistinctAttributeValuesAvailable("test", ci1, 4));
		System.out.println(pKB.getPerformanceSamplesForIndividualComponent("test", comp));

		assertTrue(true);
	}

}
