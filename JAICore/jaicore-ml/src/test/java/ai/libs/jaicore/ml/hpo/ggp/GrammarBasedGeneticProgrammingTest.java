package ai.libs.jaicore.ml.hpo.ggp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.model.Parameter;
import ai.libs.jaicore.components.model.SoftwareConfigurationProblem;
import ai.libs.jaicore.ml.hpo.ggp.GrammarBasedGeneticProgramming.GGPSolutionCandidate;

public class GrammarBasedGeneticProgrammingTest extends ATest {

	private static SoftwareConfigurationProblem<Double> input;

	@BeforeAll
	public static void setup() {
		Collection<IComponent> components = new ArrayList<>();
		Component a = new Component("A");
		a.addParameter(new Parameter("p1", new NumericParameterDomain(false, 0.0, 100.0), 50.0));
		components.add(a);

		String requiredInterface = "A";
		IObjectEvaluator<IComponentInstance, Double> evaluator = new IObjectEvaluator<IComponentInstance, Double>() {
			@Override
			public Double evaluate(final IComponentInstance object) throws InterruptedException, ObjectEvaluationFailedException {
				double p1 = Double.parseDouble(object.getParameterValues().get("p1"));
				return 0.5 * Math.pow(p1 - 4.5, 2);
			}
		};

		input = new SoftwareConfigurationProblem<>(components, requiredInterface, evaluator);
	}

	@Test
	public void randomEvalTest() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		GrammarBasedGeneticProgramming ggp = new GrammarBasedGeneticProgramming(input);
		GGPSolutionCandidate sol = ggp.call();
		this.getLogger().info("Found solution with score {}: {}", sol.getScore(), sol.getComponentInstance());
		assertTrue(sol.getScore() < 0.01);
	}

}
