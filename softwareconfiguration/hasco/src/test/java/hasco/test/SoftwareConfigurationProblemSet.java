package hasco.test;

import java.io.File;
import java.io.IOException;

import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.serialization.UnresolvableRequiredInterfaceException;
import jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;

public class SoftwareConfigurationProblemSet extends AAlgorithmTestProblemSet<RefinementConfiguredSoftwareConfigurationProblem<Double>> {

	public SoftwareConfigurationProblemSet() {
		super("Software Configuration");
	}

	@Override
	public RefinementConfiguredSoftwareConfigurationProblem<Double> getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		try {
			return new RefinementConfiguredSoftwareConfigurationProblem<>(new File("testrsc/simpleproblem.json"), "IFace", n -> 0.0);
		} catch (UnresolvableRequiredInterfaceException | IOException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	public RefinementConfiguredSoftwareConfigurationProblem<Double> getDependencyProblemInput() throws AlgorithmTestProblemSetCreationException {
		try {
			return new RefinementConfiguredSoftwareConfigurationProblem<>(new File("testrsc/problemwithdependencies.json"), "IFace", n -> 0.0);
		} catch (UnresolvableRequiredInterfaceException | IOException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	@Override
	public RefinementConfiguredSoftwareConfigurationProblem<Double> getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		try {
			return new RefinementConfiguredSoftwareConfigurationProblem<>(new File("testrsc/difficultproblem.json"), "IFace", n -> 0.0);
		} catch (UnresolvableRequiredInterfaceException | IOException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}
}
