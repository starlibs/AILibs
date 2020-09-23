package ai.libs.jaicore.ml.hpo.hyperband;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.junit.BeforeClass;
import org.junit.Test;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.model.Parameter;
import ai.libs.jaicore.ml.core.evaluation.evaluator.IMultiFidelityObjectEvaluator;
import ai.libs.jaicore.ml.hpo.multifidelity.MultiFidelitySoftwareConfigurationProblem;
import ai.libs.jaicore.ml.hpo.multifidelity.hyperband.Hyperband;
import ai.libs.jaicore.ml.hpo.multifidelity.hyperband.Hyperband.HyperbandSolutionCandidate;
import ai.libs.jaicore.ml.hpo.multifidelity.hyperband.IHyperbandConfig;

public class HyperBandTest {

	private static MultiFidelitySoftwareConfigurationProblem<Double> input;

	@BeforeClass
	public static void setup() {
		Collection<IComponent> components = new ArrayList<>();
		Component a = new Component("A");
		a.addParameter(new Parameter("p1", new NumericParameterDomain(false, 0.0, 100.0), 50.0));
		components.add(a);

		String requiredInterface = "A";
		IMultiFidelityObjectEvaluator<IComponentInstance, Double> evaluator = new IMultiFidelityObjectEvaluator<IComponentInstance, Double>() {
			@Override
			public double getMaxBudget() {
				return 5.0;
			}

			@Override
			public double getMinBudget() {
				return 1.0;
			}

			@Override
			public Double evaluate(final IComponentInstance t, final double budget) throws InterruptedException, ObjectEvaluationFailedException {
				return new Random().nextDouble();
			}
		};

		input = new MultiFidelitySoftwareConfigurationProblem<>(components, requiredInterface, evaluator);
	}

	@Test
	public void testHyperBandRunSingleThreaded() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		this.runHyperbandTest(ConfigFactory.create(IHyperbandConfig.class));
	}

	@Test
	public void testHyperBandRunMultiThreaded() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		IHyperbandConfig config = ConfigFactory.create(IHyperbandConfig.class);
		config.setProperty(IHyperbandConfig.K_CPUS, "4");
		this.runHyperbandTest(config);
	}

	private void runHyperbandTest(final IHyperbandConfig config) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		Hyperband hb = new Hyperband(config, input);
		HyperbandSolutionCandidate result = hb.call();
		assertNotNull("Returned candidate is not a solution", result);
		assertEquals("A", result.getComponentInstance().getComponent().getName());
	}
}
