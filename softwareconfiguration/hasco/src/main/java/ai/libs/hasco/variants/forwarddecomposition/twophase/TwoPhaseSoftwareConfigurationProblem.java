package ai.libs.hasco.variants.forwarddecomposition.twophase;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import ai.libs.hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.hasco.core.SoftwareConfigurationProblem;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.model.Parameter;
import ai.libs.hasco.model.ParameterRefinementConfiguration;
import ai.libs.jaicore.basic.IObjectEvaluator;

public class TwoPhaseSoftwareConfigurationProblem extends RefinementConfiguredSoftwareConfigurationProblem<Double> {
	private final IObjectEvaluator<ComponentInstance, Double> selectionBenchmark;

	public TwoPhaseSoftwareConfigurationProblem(File configurationFile, String requiredInterface, IObjectEvaluator<ComponentInstance, Double> compositionEvaluator,
			IObjectEvaluator<ComponentInstance, Double> selectionBenchmark) throws IOException {
		super(configurationFile, requiredInterface, compositionEvaluator);
		this.selectionBenchmark = selectionBenchmark;
	}

	public TwoPhaseSoftwareConfigurationProblem(SoftwareConfigurationProblem<Double> coreProblem, Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig,
			IObjectEvaluator<ComponentInstance, Double> selectionBenchmark) {
		super(coreProblem, paramRefinementConfig);
		this.selectionBenchmark = selectionBenchmark;
	}

	public IObjectEvaluator<ComponentInstance, Double> getSelectionBenchmark() {
		return selectionBenchmark;
	}
}
