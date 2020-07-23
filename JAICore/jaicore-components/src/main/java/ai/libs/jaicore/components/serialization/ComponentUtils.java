package ai.libs.jaicore.components.serialization;

import java.util.Collection;

import ai.libs.jaicore.components.model.Component;

public class ComponentUtils {

	private ComponentUtils() {
		/* avoids instantiation */
	}

	public static Component getComponentByName(final String componentName, final Collection<Component> components) throws ComponentNotFoundException {
		for (Component component : components) {
			if (component.getName().equals(componentName)) {
				return component;
			}
		}

		throw new ComponentNotFoundException("No Component with this name loaded: " + componentName);
	}
}
