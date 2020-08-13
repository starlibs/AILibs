package ai.libs.jaicore.ml.hpo.hyperband;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.SoftwareConfigurationProblem;

public class MultiFidelitySoftwareConfigurationProblem<V extends Comparable<V>> extends SoftwareConfigurationProblem<V> {

	public MultiFidelitySoftwareConfigurationProblem(final File configurationFile, final String requiredInerface, final IMultiFidelityObjectEvaluator<ComponentInstance, V> compositionEvaluator) throws IOException {
		super(configurationFile, requiredInerface, compositionEvaluator);
	}

	public MultiFidelitySoftwareConfigurationProblem(final Collection<Component> components, final String requiredInterface, final IMultiFidelityObjectEvaluator<ComponentInstance, V> compositionEvaluator) {
		super(components, requiredInterface, compositionEvaluator);
	}

	public MultiFidelitySoftwareConfigurationProblem(final SoftwareConfigurationProblem<V> problem) {
		super(problem);
	}

	@Override
	public IMultiFidelityObjectEvaluator<ComponentInstance, V> getCompositionEvaluator() {
		return (IMultiFidelityObjectEvaluator<ComponentInstance, V>) super.getCompositionEvaluator();
	}

}
