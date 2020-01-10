package ai.libs.hasco.variants.forwarddecomposition.twophase;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.api4.java.common.attributedobjects.IObjectEvaluator;

import ai.libs.hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.hasco.core.SoftwareConfigurationProblem;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.model.Parameter;
import ai.libs.hasco.model.ParameterRefinementConfiguration;

public class TwoPhaseSoftwareConfigurationProblem extends RefinementConfiguredSoftwareConfigurationProblem<Double> {
	private final IObjectEvaluator<ComponentInstance, Double> selectionBenchmark;

	public TwoPhaseSoftwareConfigurationProblem(final File configurationFile, final String requiredInterface, final IObjectEvaluator<ComponentInstance, Double> compositionEvaluator,
			final IObjectEvaluator<ComponentInstance, Double> selectionBenchmark) throws IOException {
		super(configurationFile, requiredInterface, compositionEvaluator);
		this.selectionBenchmark = selectionBenchmark;
	}

	public TwoPhaseSoftwareConfigurationProblem(final SoftwareConfigurationProblem<Double> coreProblem, final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig,
			final IObjectEvaluator<ComponentInstance, Double> selectionBenchmark) {
		super(coreProblem, paramRefinementConfig);
		this.selectionBenchmark = selectionBenchmark;
	}

	public IObjectEvaluator<ComponentInstance, Double> getSelectionBenchmark() {
		return this.selectionBenchmark;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.selectionBenchmark == null) ? 0 : this.selectionBenchmark.hashCode());
		return result;
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
		TwoPhaseSoftwareConfigurationProblem other = (TwoPhaseSoftwareConfigurationProblem) obj;
		if (this.selectionBenchmark == null) {
			if (other.selectionBenchmark != null) {
				return false;
			}
		} else if (!this.selectionBenchmark.equals(other.selectionBenchmark)) {
			return false;
		}
		return true;
	}
}
