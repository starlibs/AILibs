package ai.libs.jaicore.ml.hpo.hyperband;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.junit.Test;

import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.model.Parameter;
import ai.libs.jaicore.ml.core.evaluation.evaluator.IMultiFidelityObjectEvaluator;
import ai.libs.jaicore.ml.hpo.multifidelity.MultiFidelitySoftwareConfigurationProblem;
import ai.libs.jaicore.ml.hpo.multifidelity.hyperband.Hyperband;
import ai.libs.jaicore.ml.hpo.multifidelity.hyperband.IHyperbandConfig;

public class HyperBandTest {

	@Test
	public void testHyperBandRun() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		IHyperbandConfig config = ConfigFactory.create(IHyperbandConfig.class);
		Collection<Component> components = new ArrayList<>();
		Component a = new Component("A");
		a.addParameter(new Parameter("p1", new NumericParameterDomain(false, 0.0, 100.0), 50.0));
		components.add(a);

		String requiredInterface = "A";
		IMultiFidelityObjectEvaluator<ComponentInstance, Double> evaluator = new IMultiFidelityObjectEvaluator<ComponentInstance, Double>() {

			@Override
			public double getMaxBudget() {
				return 5.0;
			}

			@Override
			public double getMinBudget() {
				return 1.0;
			}

			@Override
			public Double evaluate(final ComponentInstance t, final double budget) throws InterruptedException, ObjectEvaluationFailedException {
				return new Random().nextDouble();
			}
		};
		MultiFidelitySoftwareConfigurationProblem<Double> input = new MultiFidelitySoftwareConfigurationProblem<>(components, requiredInterface, evaluator);
		Hyperband hb = new Hyperband(config, input);
		hb.call();
	}

}
