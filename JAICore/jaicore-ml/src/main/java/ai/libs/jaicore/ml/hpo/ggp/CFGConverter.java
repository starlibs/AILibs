package ai.libs.jaicore.ml.hpo.ggp;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IParameter;
import ai.libs.jaicore.components.api.IRequiredInterfaceDefinition;
import ai.libs.jaicore.components.exceptions.ComponentNotFoundException;
import ai.libs.jaicore.components.model.CategoricalParameterDomain;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.ComponentUtil;
import ai.libs.jaicore.components.model.NumericParameterDomain;

public class CFGConverter {

	private final Collection<? extends IComponent> components;
	private final String requestedInterface;

	public CFGConverter(final Collection<? extends IComponent> components, final String requestedInterface) {
		this.components = components;
		this.requestedInterface = requestedInterface;
	}

	public String toGrammar() {
		StringBuilder sb = new StringBuilder();
		Collection<? extends IComponent> matchingComponents = ComponentUtil.getComponentsProvidingInterface(this.components, this.requestedInterface);
		Map<String, String> productions = new HashMap<>();
		sb.append("<START> ::= ").append(this.componentsToOrListOfNonTerminals(matchingComponents)).append("\n");
		for (IComponent component : matchingComponents) {
			this.addComponentProductions(this.components, component, productions);
		}
		productions.values().stream().forEach(sb::append);
		return sb.toString();
	}

	private String componentsToOrListOfNonTerminals(final Collection<? extends IComponent> components) {
		return components.stream().map(x -> "<" + x.getName() + ">").collect(Collectors.joining(" | "));
	}

	private void addComponentProductions(final Collection<? extends IComponent> components, final IComponent component, final Map<String, String> productions) {
		StringBuilder compProduction = new StringBuilder();
		String compNT = "<" + component.getName() + ">";
		if (productions.containsKey(compNT)) {
			return;
		}
		compProduction.append(compNT).append(" ::= ").append(component.getName());

		for (IParameter param : component.getParameters()) {
			String nsParam = component.getName() + "." + param.getName();
			String paramNT = "<" + nsParam + ">";
			compProduction.append(" ").append(nsParam).append(" ").append(paramNT);

			if (param.getDefaultDomain() instanceof NumericParameterDomain) {
				NumericParameterDomain dom = (NumericParameterDomain) param.getDefaultDomain();
				if (dom.isInteger()) {
					productions.put(paramNT, paramNT + " ::= RANDINT_TYPE0(" + (int) dom.getMin() + "," + (int) dom.getMax() + ")\n");
				} else {
					productions.put(paramNT, paramNT + " ::= RANDFLOAT(" + dom.getMin() + "," + dom.getMax() + ")\n");
				}
			} else if (param.getDefaultDomain() instanceof CategoricalParameterDomain) {
				CategoricalParameterDomain dom = (CategoricalParameterDomain) param.getDefaultDomain();
				productions.put(paramNT, paramNT + " ::= " + Arrays.stream(dom.getValues()).map(x -> x.contains(" ") ? x.replaceAll(" ", "_") : x).collect(Collectors.joining(" | ")) + "\n");
			}
		}

		for (IRequiredInterfaceDefinition requiredInterface : component.getRequiredInterfaces()) {
			String nsI = component.getName() + "." + requiredInterface.getId();
			String reqINT = "<" + requiredInterface.getName() + ">";
			compProduction.append(" ").append(nsI).append(" ").append(reqINT);
			if (!productions.containsKey(reqINT)) {
				Collection<? extends IComponent> componentsMatching = ComponentUtil.getComponentsProvidingInterface(components, requiredInterface.getName());
				productions.put(reqINT, reqINT + " ::= " + this.componentsToOrListOfNonTerminals(componentsMatching) + "\n");
				componentsMatching.stream().forEach(c -> this.addComponentProductions(components, c, productions));
			}
		}
		compProduction.append("\n");
		productions.put(compNT, compProduction.toString());
	}

	public ComponentInstance grammarStringToComponentInstance(final String grammarString) throws ComponentNotFoundException {
		String[] tokens = grammarString.split(" ");
		Map<String, String> paramValues = new HashMap<>();
		for (int i = 1; i < tokens.length; i = i + 2) {
			paramValues.put(tokens[i], tokens[i + 1].contains("_") ? tokens[i + 1].replaceAll("_", " ") : tokens[i + 1]);
		}
		return this.buildComponentInstanceFromMap(tokens[0], paramValues);
	}

	private ComponentInstance buildComponentInstanceFromMap(final String componentName, final Map<String, String> values) throws ComponentNotFoundException {
		Map<String, String> parameters = new HashMap<>();
		Map<String, List<IComponentInstance>> reqIs = new HashMap<>();

		ComponentInstance root = new ComponentInstance(ComponentUtil.getComponentByName(componentName, this.components), parameters, reqIs);
		// reconstruct required interfaces
		for (IRequiredInterfaceDefinition reqI : root.getComponent().getRequiredInterfaces()) {
			root.getSatisfactionOfRequiredInterfaces().put(reqI.getId(), Arrays.asList(this.buildComponentInstanceFromMap(values.get(componentName + "." + reqI.getId()), values)));
		}
		// reconstruct param values
		for (IParameter param : root.getComponent().getParameters()) {
			root.getParameterValues().put(param.getName(), values.get(componentName + "." + param.getName()));
		}
		return root;
	}

}
