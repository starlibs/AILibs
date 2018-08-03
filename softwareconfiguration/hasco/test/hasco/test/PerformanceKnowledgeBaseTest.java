package hasco.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import hasco.knowledgebase.PerformanceKnowledgeBase;
import hasco.model.CategoricalParameterDomain;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.serialization.ComponentLoader;
import jaicore.basic.SQLAdapter;

public class PerformanceKnowledgeBaseTest {

	
	@Test
	public void test() {
		PerformanceKnowledgeBase pKB = new PerformanceKnowledgeBase();
		Component comp = new Component("TestComponent");
		NumericParameterDomain domain1 = new NumericParameterDomain(false, 0.0, 5.0);
		Parameter param1 = new Parameter("Param1", domain1, 3);
		ArrayList<String> values = new ArrayList<String>();
		values.add("Val1");
		values.add("Val2");
		values.add("Val3");
		CategoricalParameterDomain domain2 = new CategoricalParameterDomain(values);
		Parameter param2 = new Parameter("Param2", domain2, 3);
		NumericParameterDomain domain3 = new NumericParameterDomain(true, 7, 19);
		Parameter param3 = new Parameter("Param3", domain3, 3);
		comp.addParameter(param1);
		comp.addParameter(param2);
		comp.addParameter(param3);
		
		HashMap<String,String> paramValues = new HashMap<String,String>();
		paramValues.put("Param1", "4.0");
		paramValues.put("Param2", "Val3");
		paramValues.put("Param3", "13");
		
		HashMap<String, ComponentInstance> satisfactionOfRequiredInterfaces = new HashMap<String,ComponentInstance>();
		
		ComponentInstance ci1 = new ComponentInstance(comp, paramValues, satisfactionOfRequiredInterfaces);
		ComponentInstance ci2 = new ComponentInstance(comp, paramValues, satisfactionOfRequiredInterfaces);
		ComponentInstance ci3 = new ComponentInstance(comp, paramValues, satisfactionOfRequiredInterfaces);
		
		System.out.println(ci1.getParameterValues());
		
		pKB.addPerformanceSample("test", ci1, 0.7754, false);
		pKB.addPerformanceSample("test", ci2, 0.1154, false);
		pKB.addPerformanceSample("test", ci3, 0.3333, false);
		
		System.out.println(pKB.createInstancesForPerformanceSamples("test", ci1));
	}

}
