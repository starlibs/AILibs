package ai.libs.softwareconfiguration.model;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.api4.java.common.attributedobjects.IObjectEvaluator;

import ai.libs.softwareconfiguration.serialization.ComponentLoader;

/**
 * In this problem, the core software configuration problem is extended by predefining how the the parameters may be refined
 *
 * @author fmohr
 *
 * @param <V>
 */
public class RefinementConfiguredSoftwareConfigurationProblem<V extends Comparable<V>> extends SoftwareConfigurationProblem<V> {
	private final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig;

	public RefinementConfiguredSoftwareConfigurationProblem(final File configurationFile, final String requiredInterface, final IObjectEvaluator<ComponentInstance, V> compositionEvaluator) throws IOException {
		super(configurationFile, requiredInterface, compositionEvaluator);
		this.paramRefinementConfig = new ComponentLoader(configurationFile).getParamConfigs();

		/* check that parameter refinements are defined for all components */
		for (Component c : this.getComponents()) {
			if (!this.paramRefinementConfig.containsKey(c)) {
				throw new IllegalArgumentException("Error in parsing config file " + configurationFile.getAbsolutePath() + ". Component " + c.getName() + " has not parameter refinement configs associated.");
			}
			for (Parameter p : c.getParameters()) {
				if (p.isNumeric() && !this.paramRefinementConfig.get(c).containsKey(p)) {
					throw new IllegalArgumentException("Error in parsing config file " + configurationFile.getAbsolutePath() + ". No refinement config was delivered for numeric parameter " + p.getName() + " of component " + c.getName());
				}
			}
		}
	}

	public RefinementConfiguredSoftwareConfigurationProblem(final SoftwareConfigurationProblem<V> coreProblem, final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig) {
		super(coreProblem);
		this.paramRefinementConfig = paramRefinementConfig;
	}

	public Map<Component, Map<Parameter, ParameterRefinementConfiguration>>  getParamRefinementConfig() {
		return this.paramRefinementConfig;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime  + ((this.paramRefinementConfig == null) ? 0 : this.paramRefinementConfig.hashCode());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		RefinementConfiguredSoftwareConfigurationProblem<?> other = (RefinementConfiguredSoftwareConfigurationProblem<?>) obj;
		if (this.paramRefinementConfig == null) {
			if (other.paramRefinementConfig != null) {
				return false;
			}
		} else if (!this.paramRefinementConfig.equals(other.paramRefinementConfig)) {
			return false;
		}
		return true;
	}
}
