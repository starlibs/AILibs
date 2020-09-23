package ai.libs.jaicore.components.serialization;

import java.util.Collection;

import ai.libs.jaicore.components.api.IComponent;

public class ComponentUtils {

	private ComponentUtils() {
		/* avoids instantiation */
	}

	public static IComponent getComponentByName(final String componentName, final Collection<? extends IComponent> components) throws ComponentNotFoundException {
		for (IComponent component : components) {
			if (component.getName().equals(componentName)) {
				return component;
			}
		}

		throw new ComponentNotFoundException("No Component with this name loaded: " + componentName);
	}
}
