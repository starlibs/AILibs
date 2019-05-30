package hasco.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;

import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import jaicore.basic.algorithm.GeneralAlgorithmTester;
import jaicore.basic.algorithm.IAlgorithm;

public abstract class SoftwareConfigurationAlgorithmTester extends GeneralAlgorithmTester {

	// creates the test data
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		List<Object> problemSets = new ArrayList<>();
		problemSets.add(new SoftwareConfigurationProblemSet());
		Object[][] data = new Object[problemSets.size()][1];
		for (int i = 0; i < data.length; i++) {
			data[i][0] = problemSets.get(i);
		}
		return Arrays.asList(data);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final IAlgorithm<?, ?> getAlgorithm(final Object problem) {
		return this.getAlgorithmForSoftwareConfigurationProblem((RefinementConfiguredSoftwareConfigurationProblem<Double>)problem);
	}

	public abstract IAlgorithm<?, ?> getAlgorithmForSoftwareConfigurationProblem(final RefinementConfiguredSoftwareConfigurationProblem<Double> problem);
}
