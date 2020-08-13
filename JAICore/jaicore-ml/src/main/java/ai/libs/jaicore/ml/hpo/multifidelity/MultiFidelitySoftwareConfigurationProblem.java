package ai.libs.jaicore.ml.hpo.multifidelity;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.SoftwareConfigurationProblem;
import ai.libs.jaicore.ml.core.evaluation.evaluator.IMultiFidelityObjectEvaluator;

/**
 * A multi fidelity software configuration problem is a software configuraiton problem but requiring the composition evaluator to
 * support multi-fidelity, i.e. evaluating a candidate with a specified amount of resources.
 *
 * @author mwever
 *
 * @param <V> A comparable value for assessing the quality of candidates, usually a Double.
 */
public class MultiFidelitySoftwareConfigurationProblem<V extends Comparable<V>> extends SoftwareConfigurationProblem<V> {

	public MultiFidelitySoftwareConfigurationProblem(final File configurationFile, final String requiredInerface, final IMultiFidelityObjectEvaluator<ComponentInstance, V> compositionEvaluator) throws IOException {
		super(configurationFile, requiredInerface, compositionEvaluator);
	}

	public MultiFidelitySoftwareConfigurationProblem(final Collection<Component> components, final String requiredInterface, final IMultiFidelityObjectEvaluator<ComponentInstance, V> compositionEvaluator) {
		super(components, requiredInterface, compositionEvaluator);
	}

	public MultiFidelitySoftwareConfigurationProblem(final MultiFidelitySoftwareConfigurationProblem<V> problem) {
		super(problem);
	}

	@Override
	public IMultiFidelityObjectEvaluator<ComponentInstance, V> getCompositionEvaluator() {
		return (IMultiFidelityObjectEvaluator<ComponentInstance, V>) super.getCompositionEvaluator();
	}

}
