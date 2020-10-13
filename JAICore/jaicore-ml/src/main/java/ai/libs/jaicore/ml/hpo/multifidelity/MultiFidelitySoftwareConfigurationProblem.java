package ai.libs.jaicore.ml.hpo.multifidelity;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
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

	public MultiFidelitySoftwareConfigurationProblem(final File configurationFile, final String requestedInterface, final IMultiFidelityObjectEvaluator<IComponentInstance, V> compositionEvaluator) throws IOException {
		super(configurationFile, requestedInterface, compositionEvaluator);
	}

	public MultiFidelitySoftwareConfigurationProblem(final Collection<IComponent> components, final String requestedInterface, final IMultiFidelityObjectEvaluator<IComponentInstance, V> compositionEvaluator) {
		super(components, requestedInterface, compositionEvaluator);
	}

	public MultiFidelitySoftwareConfigurationProblem(final MultiFidelitySoftwareConfigurationProblem<V> problem) {
		super(problem);
	}

	@Override
	public IMultiFidelityObjectEvaluator<IComponentInstance, V> getCompositionEvaluator() {
		return (IMultiFidelityObjectEvaluator<IComponentInstance, V>) super.getCompositionEvaluator();
	}

}
