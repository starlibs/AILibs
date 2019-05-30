package hasco.knowledgebase;

import java.util.Comparator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.model.Component;
import hasco.model.Parameter;

/**
 * Comparator which compares parameters according to their importance using the
 * FANOVAImportanceEstimator.
 * 
 * @author jmhansel
 *
 */
public class FANOVAWarmstartComparator implements Comparator<Parameter> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FANOVAWarmstartComparator.class);

	private Map<String, Double> importanceValues;
	private IParameterImportanceEstimator importanceEstimator;

	public FANOVAWarmstartComparator(IParameterImportanceEstimator importanceEstimator, Component component) {
		this.importanceEstimator = importanceEstimator;
		this.importanceValues = this.importanceEstimator.computeImportanceForSingleComponent(component);
		LOGGER.debug("importance values: {}", importanceValues);
	}

	/**
	 * Compares parameters according to their importance values
	 */
	@Override
	public int compare(Parameter o1, Parameter o2) {
		if (importanceValues == null) {
			return 0;
		}
		LOGGER.debug("{} value: {}", o1, importanceValues.get(o1.toString()));
		// We want the parameters to be sorted in descending order according to their importance
		if (importanceValues.get(o1.getName()) < importanceValues.get(o2.getName())) {
			return 1;
		}
		if (importanceValues.get(o1.getName()) > importanceValues.get(o2.getName())) {
			return -1;
		}
		return 0;
	}

}
