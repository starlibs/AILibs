package ai.libs.hyperopt.optimizer.pcs.converter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.CategoricalParameterDomain;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentUtil;
import ai.libs.hasco.model.IParameterDomain;
import ai.libs.hasco.model.NumericParameterDomain;
import ai.libs.hasco.model.Parameter;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.basic.sets.SetUtil;

/**
 * For converting HASCO format to PCS format
 *
 * @author kadirayk
 *
 */
public class ComponentToPCSConverterUtil {
	private static final Logger logger = LoggerFactory.getLogger(ComponentToPCSConverterUtil.class);

	private static Map<String, List<String>> componentConditionals;

	// mapping from artifical names to original parameter names
	private static Map<String, String> dependendParameterMap;

	private Map<String, List<String>> conditions = new HashMap<>();

	public static Map<String, String> getParameterMapping() {
		return dependendParameterMap;
	}

	private final Collection<Component> components;
	private final String requiredInterface;

	public ComponentToPCSConverterUtil(final Collection<Component> components, final String requiredInterface) {
		this.components = components;
		this.requiredInterface = requiredInterface;
	}

	public void toPCSFile(final String outputPath) throws IOException {
		this.toPCSFile(new File(outputPath));
	}

	public void toPCSFile(final File outputFile) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
			bw.write(this.toPCS());
		}
	}

	public String toPCS() {
		if (ComponentUtil.hasCycles(this.components, this.requiredInterface)) {
			throw new IllegalArgumentException("Cyclic dependencies or potentially infinite recursions are not supported.");
		}
		Collection<Component> affectedComponents = ComponentUtil.getAffectedComponents(this.components, this.requiredInterface);

		StringBuilder sb = new StringBuilder();
		StringBuilder conditions = new StringBuilder();
		affectedComponents.stream().map(x -> this.convertComponent(x, affectedComponents, conditions) + "\n").forEach(sb::append);

		Collection<Component> rootComponents = ComponentUtil.getComponentsProvidingInterface(affectedComponents, this.requiredInterface);

		if (rootComponents.size() > 1) {
			// create virtual root switch:
			List<String> rootValues = rootComponents.stream().map(x -> x.getName()).collect(Collectors.toList());
			sb.append(this.convertCategoricalParameter("root", "Component", rootValues, rootValues.get(0))).append("\n").append("\n");
			rootComponents.stream().forEach(x -> sb.append(this.generateComponentCondition("root", "Component", x)));
		}

		sb.append(conditions);

		return sb.toString();
	}

	public String generateComponentCondition(final String componentName, final String entity, final Component dependent) {
		StringBuilder sb = new StringBuilder();
		dependent.getParameters().stream().map(x -> this.getNamespacedName(dependent.getName(), x.getName()) + " | " + this.getNamespacedName(componentName, entity) + " in {" + dependent.getName() + "}\n").forEach(sb::append);
		return sb.toString();
	}

	public String convertComponent(final Component component, final Collection<Component> components, final StringBuilder conditions) {
		StringBuilder sb = new StringBuilder();
		component.getParameters().stream().map(x -> this.convertParameter(component, x) + "\n").filter(x -> !x.trim().equals("null")).forEach(sb::append);
		for (Entry<String, String> reqI : component.getRequiredInterfaces().entrySet()) {
			Collection<Component> reqComponents = ComponentUtil.getComponentsProvidingInterface(components, reqI.getValue());
			List<String> availableComponentsForInterface = reqComponents.stream().map(x -> x.getName()).collect(Collectors.toList());
			sb.append(this.convertCategoricalParameter(component.getName(), reqI.getKey(), availableComponentsForInterface, availableComponentsForInterface.get(0)));

			String entity = reqI.getKey();
			reqComponents.stream().forEach(x -> conditions.append(this.generateComponentCondition(component.getName(), entity, x)).append("\n"));
		}

		return sb.toString();
	}

	public String convertParameter(final Component component, final Parameter parameter) {
		if (parameter.getDefaultDomain() instanceof NumericParameterDomain) {
			return this.convertNumericParameter(component.getName(), parameter);
		} else if (parameter.getDefaultDomain() instanceof CategoricalParameterDomain) {
			return this.convertCategoricalParameter(component.getName(), parameter);
		} else {
			throw new IllegalArgumentException("Unsupported parameter domain " + parameter.getDefaultDomain().getClass().getSimpleName());
		}
	}

	private static String handleDependencyConditional(final String name, final Pair<Parameter, IParameterDomain> post, final Pair<Parameter, IParameterDomain> pre, final Map<String, Integer> dependedParameterCounts) {
		int lastDot = name.lastIndexOf(".");
		String compName = name.substring(0, lastDot);
		StringBuilder cond = new StringBuilder(name);
		String artificialName = "opt" + dependedParameterCounts.get(post.getX().getName()) + "-" + post.getX().getName();
		dependendParameterMap.put(artificialName, post.getX().getName());
		cond.append(".").append(artificialName).append("|");
		cond.append(name).append(".");
		cond.append(pre.getX().getName());
		if (pre.getY() instanceof CategoricalParameterDomain) {
			CategoricalParameterDomain domain = (CategoricalParameterDomain) pre.getY();
			cond.append(" in {");
			for (String val : domain.getValues()) {
				cond.append(val).append(",");
			}
			cond.replace(cond.length() - 1, cond.length(), "");
			cond.append("}");
		} else if (pre.getY() instanceof NumericParameterDomain) {
			NumericParameterDomain domain = (NumericParameterDomain) pre.getY();
			cond.append(" > ").append(domain.getMin());
			cond.append(" && ").append(pre.getX().getName()).append(" < ").append(domain.getMax());
		}
		return cond.toString();
	}

	/**
	 * Converts a numeric parameter into a PCS parameter description.
	 * @param componentName The name of the component of this parameter to be converted.
	 * @param parameter The numeric parameter to be converted.
	 * @return A string description in the PCS format of this numeric parameter.
	 */
	public String convertNumericParameter(final String componentName, final Parameter parameter) {
		this.sanityCheck(componentName, parameter, NumericParameterDomain.class);

		StringBuilder sb = new StringBuilder(this.getNamespacedName(componentName, parameter.getName()));
		NumericParameterDomain domain = (NumericParameterDomain) parameter.getDefaultDomain();

		final double[] minMaxDef = { domain.getMin(), domain.getMax(), (Double) parameter.getDefaultValue() };
		if (minMaxDef[0] >= minMaxDef[1]) {
			logger.warn("Parameter {} of component {} is ignored as the min max range is not of positive size.", parameter.getName(), componentName);
			return null;
		}
		if (!(minMaxDef[0] <= minMaxDef[2] && minMaxDef[1] >= minMaxDef[2])) {
			logger.error("The default value of parameter {} in component {} is not part of the valid value domain.", parameter.getName(), componentName);
			throw new IllegalArgumentException("Default value does not lie in the min max range.");
		}

		// TODO: determine whether a parameter is log-scale refinement. For now, always do ordinal parameter refinement.
		boolean logScale = false;

		String[] minMaxDefString = Arrays.stream(minMaxDef).mapToObj(x -> domain.isInteger() ? Integer.toString(((int) x)) : Double.toString(x)).toArray(size -> new String[size]);
		sb.append(" [").append(minMaxDefString[0]).append(",").append(minMaxDefString[1]).append("][").append(minMaxDefString[2]).append("]");

		// if integer append indicator for integers
		if (domain.isInteger()) {
			sb.append("i");
		} else if (logScale) {
			sb.append("l");
		}

		return sb.toString();
	}

	/**
	 * Converts a categorical parameter into a pcs style parameter.
	 * @param componentName The component of the parameter to be converted.
	 * @param parameter The categorical parameter to be converted.
	 * @return The string representing the pcs definition of the give categorical parameter.
	 */
	public String convertCategoricalParameter(final String componentName, final Parameter parameter) {
		this.sanityCheck(componentName, parameter, CategoricalParameterDomain.class);
		CategoricalParameterDomain domain = (CategoricalParameterDomain) parameter.getDefaultDomain();
		return this.convertCategoricalParameter(componentName, parameter.getName(), Arrays.asList(domain.getValues()), parameter.getDefaultValue() + "");
	}

	private String convertCategoricalParameter(final String componentName, final String entityName, final List<String> values, final String defaultValue) {
		if (values.isEmpty()) {
			logger.warn("The list of values for the entity {} of component {} is empty and thus this entity is going to be ignored.", entityName, componentName);
			return null;
		}

		if (!values.contains(defaultValue)) {
			throw new IllegalArgumentException("The categorical paramter domain " + values + " of " + this.getNamespacedName(componentName, entityName) + " does not contain the default value " + defaultValue + ".");
		}

		StringBuilder sb = new StringBuilder(this.getNamespacedName(componentName, entityName)).append(" ");
		sb.append("{").append(SetUtil.implode(values, ",")).append("}");
		sb.append("[").append(defaultValue).append("]");
		return sb.toString();
	}

	private String getNamespacedName(final String componentName, final String entityName) {
		return new StringBuilder(componentName).append(".").append(entityName).toString();
	}

	private void sanityCheck(final String componentName, final Parameter parameter, final Class<?> domainClass) {
		if (!domainClass.isAssignableFrom(parameter.getDefaultDomain().getClass())) {
			logger.error("Tried to convert parameter {} of component {} with the wrong parameter domain {} instead of {}", parameter.getName(), componentName, parameter.getClass().getSimpleName(), domainClass.getSimpleName());
			throw new IllegalArgumentException("The default domain of the given parameter is not a " + domainClass.getSimpleName());
		}
	}
}
