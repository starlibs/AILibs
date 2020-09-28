package ai.libs.hasco.twophase;

import java.io.File;
import java.io.IOException;

import org.api4.java.common.attributedobjects.IObjectEvaluator;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.INumericParameterRefinementConfigurationMap;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.components.model.SoftwareConfigurationProblem;

public class TwoPhaseSoftwareConfigurationProblem extends RefinementConfiguredSoftwareConfigurationProblem<Double> {
	private final IObjectEvaluator<IComponentInstance, Double> selectionBenchmark;

	public TwoPhaseSoftwareConfigurationProblem(final File configurationFile, final String requiredInterface, final IObjectEvaluator<IComponentInstance, Double> compositionEvaluator,
			final IObjectEvaluator<IComponentInstance, Double> selectionBenchmark) throws IOException {
		super(configurationFile, requiredInterface, compositionEvaluator);
		this.selectionBenchmark = selectionBenchmark;
	}

	public TwoPhaseSoftwareConfigurationProblem(final SoftwareConfigurationProblem<Double> coreProblem, final INumericParameterRefinementConfigurationMap paramRefinementConfig,
			final IObjectEvaluator<IComponentInstance, Double> selectionBenchmark) {
		super(coreProblem, paramRefinementConfig);
		this.selectionBenchmark = selectionBenchmark;
	}

	public IObjectEvaluator<IComponentInstance, Double> getSelectionBenchmark() {
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
