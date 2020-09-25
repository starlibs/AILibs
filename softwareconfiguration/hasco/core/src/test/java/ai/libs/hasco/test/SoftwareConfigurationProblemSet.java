package ai.libs.hasco.test;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import ai.libs.jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.components.exceptions.UnresolvableRequiredInterfaceException;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;

public class SoftwareConfigurationProblemSet extends AAlgorithmTestProblemSet<RefinementConfiguredSoftwareConfigurationProblem<Double>> {

	private static final String PATH_TO_SOFTWARECONFIG = "../../../JAICore/jaicore-components/";
	private final Random random = new Random(0);

	public SoftwareConfigurationProblemSet() {
		super("Software Configuration");
	}

	@Override
	public RefinementConfiguredSoftwareConfigurationProblem<Double> getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		try {
			return new RefinementConfiguredSoftwareConfigurationProblem<>(new File(PATH_TO_SOFTWARECONFIG + "testrsc/simpleproblem.json"), "IFace", n -> this.random.nextDouble() * 0.1);
		} catch (UnresolvableRequiredInterfaceException | IOException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	public RefinementConfiguredSoftwareConfigurationProblem<Double> getDependencyProblemInput() throws AlgorithmTestProblemSetCreationException {
		try {
			return new RefinementConfiguredSoftwareConfigurationProblem<>(new File(PATH_TO_SOFTWARECONFIG + "testrsc/problemwithdependencies.json"), "IFace", n -> this.random.nextDouble() * 0.1);
		} catch (UnresolvableRequiredInterfaceException | IOException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	public RefinementConfiguredSoftwareConfigurationProblem<Double> getSimpleProblemInputWithTwoComponents() throws AlgorithmTestProblemSetCreationException {
		try {
			return new RefinementConfiguredSoftwareConfigurationProblem<>(new File(PATH_TO_SOFTWARECONFIG + "testrsc/simpleproblemwithtwocomponents.json"), "IFace", n -> this.random.nextDouble() * 0.1);
		} catch (UnresolvableRequiredInterfaceException | IOException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	public RefinementConfiguredSoftwareConfigurationProblem<Double> getSimpleRecursiveProblemInput() throws AlgorithmTestProblemSetCreationException {
		try {
			return new RefinementConfiguredSoftwareConfigurationProblem<>(new File(PATH_TO_SOFTWARECONFIG + "testrsc/simplerecursiveproblem.json"), "IFace", n -> this.random.nextDouble() * 0.1);
		} catch (UnresolvableRequiredInterfaceException | IOException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	@Override
	public RefinementConfiguredSoftwareConfigurationProblem<Double> getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		try {
			return new RefinementConfiguredSoftwareConfigurationProblem<>(new File(PATH_TO_SOFTWARECONFIG + "testrsc/difficultproblem.json"), "IFace", n -> this.random.nextDouble() * 0.1);
		} catch (UnresolvableRequiredInterfaceException | IOException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}
}
