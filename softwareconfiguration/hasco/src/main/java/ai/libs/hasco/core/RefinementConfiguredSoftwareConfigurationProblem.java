package ai.libs.hasco.core;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.model.Parameter;
import ai.libs.hasco.model.ParameterRefinementConfiguration;
import ai.libs.hasco.serialization.ComponentLoader;
import ai.libs.hasco.serialization.UnresolvableRequiredInterfaceException;
import ai.libs.jaicore.basic.IObjectEvaluator;

/**
 * In this problem, the core software configuration problem is extended by predefining how the the parameters may be refined
 *
 * @author fmohr
 *
 * @param <V>
 */
public class RefinementConfiguredSoftwareConfigurationProblem<V extends Comparable<V>> extends SoftwareConfigurationProblem<V> {
	private final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig;

	public RefinementConfiguredSoftwareConfigurationProblem(final File configurationFile, final String requiredInterface, final IObjectEvaluator<ComponentInstance, V> compositionEvaluator) throws IOException, UnresolvableRequiredInterfaceException {
		super(configurationFile, requiredInterface, compositionEvaluator);
		this.paramRefinementConfig = new ComponentLoader(configurationFile).getParamConfigs();
	}

	public RefinementConfiguredSoftwareConfigurationProblem(final SoftwareConfigurationProblem<V> coreProblem, final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig) {
		super(coreProblem);
		this.paramRefinementConfig = paramRefinementConfig;
	}

	public Map<Component, Map<Parameter, ParameterRefinementConfiguration>>  getParamRefinementConfig() {
		return paramRefinementConfig;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((paramRefinementConfig == null) ? 0 : paramRefinementConfig.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RefinementConfiguredSoftwareConfigurationProblem other = (RefinementConfiguredSoftwareConfigurationProblem) obj;
		if (paramRefinementConfig == null) {
			if (other.paramRefinementConfig != null) {
				return false;
			}
		} else if (!paramRefinementConfig.equals(other.paramRefinementConfig)) {
			return false;
		}
		return true;
	}
}
