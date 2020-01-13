package ai.libs.hyperopt;

import java.util.Collection;

import ai.libs.hasco.model.Component;

/**
 *
 * @author kadirayk
 *
 */
public class PCSBasedOptimizerInput {

	private Collection<Component> components;

	private String requestedInterface;

	public PCSBasedOptimizerInput(final Collection<Component> components, final String requestedInterface) {
		this.components = components;
		this.requestedInterface = requestedInterface;
	}

	public Collection<Component> getComponents() {
		return this.components;
	}

	public String getRequestedInterface() {
		return this.requestedInterface;
	}

}
