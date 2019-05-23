package hasco.knowledgebase;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import hasco.model.Component;
import hasco.model.Parameter;
import jaicore.basic.SQLAdapter;

/**
 * Comparator which compares parameters according to their importance using the
 * FANOVAImportanceEstimator.
 * 
 * @author jmhansel
 *
 */
public class FANOVAWarmstartComparator implements Comparator<Parameter> {

	private Map<String, Double> importanceValues;
	private IParameterImportanceEstimator importanceEstimator;

	public FANOVAWarmstartComparator(IParameterImportanceEstimator importanceEstimator,
			Component component) {
		this.importanceEstimator = importanceEstimator;
		this.importanceValues = this.importanceEstimator.computeImportanceForSingleComponent(component);
		System.out.println("importance values: " + importanceValues);
	}

	/**
	 * Compares parameters according to their importance values
	 */
	@Override
	public int compare(Parameter o1, Parameter o2) {
		if (importanceValues == null)
			return 0;
		System.out.println(o1.toString() + " value: " + importanceValues.get(o1));
		if (importanceValues == null)
			return 0;
		// We want the parameters to be sorted in descending order according to their
		// importance
		if (importanceValues.get(o1.getName()) < importanceValues.get(o2.getName()))
			return 1;
		if (importanceValues.get(o1.getName()) > importanceValues.get(o2.getName()))
			return -1;
		return 0;
	}

}
