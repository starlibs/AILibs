package ai.libs.hasco.test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.api4.java.algorithm.IAlgorithm;
import org.junit.jupiter.params.provider.Arguments;

import ai.libs.jaicore.basic.algorithm.GeneralAlgorithmTester;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;

public abstract class SoftwareConfigurationAlgorithmTester extends GeneralAlgorithmTester {

	public static Stream<Arguments> getProblemSets() {
		List<Arguments> problemSets = new ArrayList<>();
		problemSets.add(Arguments.of(new SoftwareConfigurationProblemSet()));
		return problemSets.stream();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final IAlgorithm<?, ?> getAlgorithm(final Object problem) {
		return this.getAlgorithmForSoftwareConfigurationProblem((RefinementConfiguredSoftwareConfigurationProblem<Double>)problem);
	}

	public abstract IAlgorithm<?, ?> getAlgorithmForSoftwareConfigurationProblem(final RefinementConfiguredSoftwareConfigurationProblem<Double> problem);
}
