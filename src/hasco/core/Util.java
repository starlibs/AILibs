package hasco.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;

import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameter;
import hasco.model.Parameter;
import jaicore.basic.SetUtil;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;

class Util {

	static Map<String, ComponentInstance> getGroundComponentsFromState(Monom state, Collection<Component> components, boolean resolveIntervals) {
		Map<String, ComponentInstance> objectMap = new HashMap<>();
		Map<String, Map<String, String>> parameterContainerMap = new HashMap<>(); // stores for each object the name of the container of each parameter
		Map<String, String> parameterValues = new HashMap<>();

		for (Literal l : state) {
			String[] params = l.getParameters().stream().map(p -> p.getName()).collect(Collectors.toList()).toArray(new String[] {});
			switch (l.getPropertyName()) {
			case "resolves":
				String parentObjectName = params[0];
				String interfaceName = params[1];
				String componentName = params[2];
				String objectName = params[3];
				Component component = components.stream().filter(c -> c.getName().equals(componentName)).findAny().get();
				ComponentInstance object = new ComponentInstance(component, new HashMap<>(), new HashMap<>());
				objectMap.put(objectName, object);
				if (!parentObjectName.equals("request"))
					objectMap.get(parentObjectName).getSatisfactionOfRequiredInterfaces().put(interfaceName, object);
				break;
			case "parameterContainer":
				if (!parameterContainerMap.containsKey(params[2]))
					parameterContainerMap.put(params[2], new HashMap<>());
				parameterContainerMap.get(params[2]).put(params[1], params[3]);
				break;
			case "val":
				parameterValues.put(params[0], params[1]);
				break;
			}
		}

		/* update the configurations of the objects */
		for (String objectName : objectMap.keySet()) {
			ComponentInstance object = objectMap.get(objectName);
			for (Parameter p : object.getComponent().getParameters()) {

				String assignedValue = parameterValues.get(parameterContainerMap.get(objectName).get(p.getName()));
				String interpretedValue = "";
				if (assignedValue != null) {
					if (p instanceof NumericParameter) {
						if (resolveIntervals) {
							NumericParameter np = (NumericParameter) p;
							List<String> vals = SetUtil.unserializeList(assignedValue);
							Interval interval = new Interval(Double.valueOf(vals.get(0)), Double.valueOf(vals.get(1)));
							if (np.isInteger())
								interpretedValue = String.valueOf((int) Math.round(interval.getBarycenter()));
							else
								interpretedValue = String.valueOf(interval.getBarycenter());
						}
						else
							interpretedValue = assignedValue;
					} else
						throw new UnsupportedOperationException("No support for parameters of type " + p.getClass().getName());
					object.getParameterValues().put(p.getName(), interpretedValue);
				}
			}
		}
		return objectMap;
	}
}
