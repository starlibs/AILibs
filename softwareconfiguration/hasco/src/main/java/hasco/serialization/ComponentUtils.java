package hasco.serialization;

import java.util.Collection;

import hasco.model.Component;

public class ComponentUtils {

	  public static Component getComponentByName(String componentName, Collection<Component> components) throws ComponentNotFoundException  {
			for (Component component : components) {
				if (component.getName().equals(componentName)) {
					return component; 
				}
			}
			
			throw new ComponentNotFoundException("No Component with this name loaded: " + componentName);
	  }
}
