package hasco.variants.forwarddecomposition.twophase;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.core.SoftwareConfigurationProblem;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import jaicore.basic.IObjectEvaluator;

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
