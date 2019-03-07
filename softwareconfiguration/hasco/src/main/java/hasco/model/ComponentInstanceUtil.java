package hasco.model;

import java.util.HashMap;
import java.util.Map;

public class ComponentInstanceUtil {

	public static boolean isValidComponentInstantiation(final ComponentInstance ci) {
		Map<Parameter, ParameterDomain> refinedDomainMap = new HashMap<>();

		for (Parameter param : ci.getComponent().getParameters()) {
			ci.getParameterValue(param);

		}

		// Util.isDependencyPremiseSatisfied(dependency, values);

		return true;
	}

}
