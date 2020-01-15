package ai.libs.hyperopt;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.CategoricalParameterDomain;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentUtil;
import ai.libs.hasco.model.NumericParameterDomain;
import ai.libs.hasco.model.Parameter;

/**
 * For converting HASCO format to PCS format
 *
 * @author kadirayk
 *
 */
public class HASCOToPCSConverter {
	private static final Logger logger = LoggerFactory.getLogger(HASCOToPCSConverter.class);

	private static Map<String, List<String>> componentConditionals = new HashMap<>();

	private HASCOToPCSConverter() {
		/* avoid instantiation */
	}

	/**
	 * PCS Files will be generated for the components in the input, generated files
	 * will be stored in the given outputDir. A separate PCS file will be generated for each Component
	 *
	 * @param input
	 * @param outputDir
	 * @throws Exception
	 */
	public static void generatePCSFile(final PCSBasedOptimizerInput input, final String outputDir) {
		Collection<Component> components = input.getComponents();
		String requestedInterface = input.getRequestedInterface();
		if (ComponentUtil.hasCycles(components, requestedInterface)) {
			throw new IllegalArgumentException("Component has cycles. Not converting to PCS");
		}
		toPCS(components, requestedInterface, outputDir);
	}

	private static void toPCS(final Collection<Component> components, final String requestedInterface, final String outputDir) {
		List<Component> componentsToGenerate = components.stream().filter(c -> c.getProvidedInterfaces().contains(requestedInterface)).collect(Collectors.toList());

		for (Component cmp : componentsToGenerate) {
			StringBuilder pcsContent = new StringBuilder();
			int lastDot = cmp.getName().lastIndexOf('.');
			String name = cmp.getName().substring(lastDot + 1);
			Set<Parameter> params = cmp.getParameters();
			Map<String, String> requiredInterfaces = cmp.getRequiredInterfaces();
			for (Map.Entry<String, String> e : requiredInterfaces.entrySet()) {
				String interfaceId = e.getKey();
				String interfaceName = e.getValue();
				pcsContent.append(handleRequiredInterfaces(cmp, interfaceId, interfaceName, components)).append(System.lineSeparator());
			}

			for (Parameter param : params) {
				if (param.getDefaultDomain() instanceof CategoricalParameterDomain) {
					pcsContent.append(handleCategorical(cmp.getName(), param)).append(System.lineSeparator());
				} else if (param.getDefaultDomain() instanceof NumericParameterDomain) {
					pcsContent.append(handleNumeric(cmp.getName(), param)).append(System.lineSeparator());
				}
			}

			pcsContent.append(handleConditionals());

			String finalContent = removeDuplicateLines(pcsContent);

			try (FileWriter fw = new FileWriter(outputDir + name + ".pcs")) {
				fw.write(finalContent);
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}

	private static String removeDuplicateLines(final StringBuilder content) {
		String[] lines = content.toString().split(System.lineSeparator());
		StringBuilder cleanContent = new StringBuilder();
		Arrays.stream(lines).forEach(l -> cleanContent.append(l).append(System.lineSeparator()));
		return cleanContent.toString();
	}

	private static String handleConditionals() {
		StringBuilder str = new StringBuilder();
		str.append("Conditionals:").append(System.lineSeparator());
		for (List<String> lines : componentConditionals.values()) {
			lines.forEach(l -> str.append(l).append(System.lineSeparator()));
			str.append(System.lineSeparator());
		}
		return str.toString();
	}

	private static String handleRequiredInterfaces(final Component requiringComponent, final String interfaceId, final String interfaceName, final Collection<Component> components) {
		List<Component> componentsProvidingTheInterface = components.stream().filter(c -> c.getProvidedInterfaces().contains(interfaceName)).collect(Collectors.toList());

		StringBuilder pcsContent = new StringBuilder(interfaceId).append(" {");
		for (Component cmp : componentsProvidingTheInterface) {
			pcsContent.append(cmp.getName()).append(",");
		}
		pcsContent.replace(pcsContent.length() - 1, pcsContent.length(), "");
		pcsContent.append("}");

		// use the first element as the default one
		pcsContent.append("[").append(componentsProvidingTheInterface.get(0).getName()).append("]").append(System.lineSeparator());

		for (Component cmp : componentsProvidingTheInterface) {
			Map<String, String> requiredInterfaces = cmp.getRequiredInterfaces();
			for (Map.Entry<String, String> e : requiredInterfaces.entrySet()) {
				String subInterfaceId = e.getKey();
				String subInterfaceName = e.getValue();
				pcsContent.append(handleRequiredInterfaces(cmp, subInterfaceId, subInterfaceName, components));
			}

			Set<Parameter> params = cmp.getParameters();
			for (Parameter param : params) {
				if (param.getDefaultDomain() instanceof CategoricalParameterDomain) {
					pcsContent.append(handleCategorical(cmp.getName(), param)).append(System.lineSeparator());
				} else if (param.getDefaultDomain() instanceof NumericParameterDomain) {
					pcsContent.append(handleNumeric(cmp.getName(), param)).append(System.lineSeparator());
				}
				StringBuilder conditional = new StringBuilder(cmp.getName()).append(".");
				conditional.append(param.getName()).append("|").append(interfaceId).append(" in {").append(cmp.getName()).append("}");
				List<String> conditionals = componentConditionals.get(requiringComponent.getName());
				if (conditionals == null) {
					conditionals = new ArrayList<>();
					conditionals.add(conditional.toString());
				} else {
					conditionals.add(conditional.toString());
				}
				componentConditionals.put(requiringComponent.getName(), conditionals);
			}
		}

		return pcsContent.toString();
	}

	private static String handleNumeric(final String componentName, final Parameter param) {
		StringBuilder pcsLine = new StringBuilder(componentName).append(".");
		String defaultValue = param.getDefaultValue().toString();
		NumericParameterDomain domain = (NumericParameterDomain) param.getDefaultDomain();
		String max = String.valueOf(domain.getMax());
		String min = String.valueOf(domain.getMin());
		String name = param.getName();
		pcsLine.append(name).append(" ");

		pcsLine.append("[").append(min).append(",").append(max).append("] ");

		pcsLine.append("[").append(defaultValue).append("]");

		return pcsLine.toString();
	}

	private static String handleCategorical(final String componentName, final Parameter param) {
		StringBuilder pcsLine = new StringBuilder(componentName).append(".");
		String defaultValue = param.getDefaultValue().toString();
		CategoricalParameterDomain domain = (CategoricalParameterDomain) param.getDefaultDomain();
		String[] values = domain.getValues();
		String name = param.getName();
		pcsLine.append(name).append(" ");

		pcsLine.append("{");
		for (String val : values) {
			pcsLine.append(val).append(",");
		}
		pcsLine.replace(pcsLine.length() - 1, pcsLine.length(), "");
		pcsLine.append("}");

		pcsLine.append("[").append(defaultValue).append("]");
		return pcsLine.toString();
	}

}
