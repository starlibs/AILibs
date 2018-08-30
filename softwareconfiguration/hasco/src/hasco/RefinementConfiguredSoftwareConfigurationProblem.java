package hasco;

import java.util.Map;

import hasco.model.Component;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;

/**
 * In this problem, the core software configuration problem is extended by predefining how the the parameters may be refined
 * 
 * @author fmohr
 *
 * @param <V>
 */
public class RefinementConfiguredSoftwareConfigurationProblem<V extends Comparable<V>> {
	private final SoftwareConfigurationProblem<V> coreProblem;
	private final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig;

	public RefinementConfiguredSoftwareConfigurationProblem(SoftwareConfigurationProblem<V> coreProblem, Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig) {
		super();
		this.coreProblem = coreProblem;
		this.paramRefinementConfig = paramRefinementConfig;
	}

	public SoftwareConfigurationProblem<V> getCoreProblem() {
		return coreProblem;
	}

	public Map<Component, Map<Parameter, ParameterRefinementConfiguration>>  getParamRefinementConfig() {
		return paramRefinementConfig;
	}
}
