package hasco.knowledgebase;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import hasco.model.Component;
import hasco.model.Parameter;
import jaicore.basic.SQLAdapter;

/**
 * Comparator which compares parameters according to their importance using the FANOVAImportanceEstimator.
 * @author jmhansel
 *
 */
public class FANOVAWarmstartComparator implements Comparator<Parameter> {

	private Map<String, Double> importanceValues;
	private PerformanceKnowledgeBase performanceKB;
	private IParameterImportanceEstimator importanceEstimator;
	
	public FANOVAWarmstartComparator(PerformanceKnowledgeBase performanceKB, String benchmarkName, Component component) {
		this.performanceKB = performanceKB;
		this.importanceEstimator = new FANOVAParameterImportanceEstimator(performanceKB, benchmarkName);
		this.importanceValues = this.importanceEstimator.computeImportanceForSingleComponent(component);
	}

	/**
	 * Compares parameters according to their importance values
	 */
	@Override
	public int compare(Parameter o1, Parameter o2) {
		if (importanceValues.get(o1.getName()) < importanceValues.get(o2.getName()))
			return -1;
		if(importanceValues.get(o1.getName()) > importanceValues.get(o2.getName()))
			return 1;
		return 0;
	}
	
}
