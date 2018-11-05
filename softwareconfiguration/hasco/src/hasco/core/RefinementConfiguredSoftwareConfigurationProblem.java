package hasco.core;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import hasco.serialization.ComponentLoader;
import jaicore.basic.IObjectEvaluator;

/**
 * In this problem, the core software configuration problem is extended by predefining how the the parameters may be refined
 * 
 * @author fmohr
 *
 * @param <V>
 */
public class RefinementConfiguredSoftwareConfigurationProblem<V extends Comparable<V>> extends SoftwareConfigurationProblem<V> {
	private final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig;

	public RefinementConfiguredSoftwareConfigurationProblem(File configurationFile, String requiredInterface, IObjectEvaluator<ComponentInstance, V> compositionEvaluator) throws IOException {
		super(configurationFile, requiredInterface, compositionEvaluator);
		this.paramRefinementConfig = new ComponentLoader(configurationFile).getParamConfigs();
	}
	
	public RefinementConfiguredSoftwareConfigurationProblem(SoftwareConfigurationProblem<V> coreProblem, Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig) {
		super(coreProblem);
		this.paramRefinementConfig = paramRefinementConfig;
	}

	public Map<Component, Map<Parameter, ParameterRefinementConfiguration>>  getParamRefinementConfig() {
		return paramRefinementConfig;
	}
}
