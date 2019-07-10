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
}
