package ai.libs.hyperopt.optimizer.pcs.converter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.BooleanParameterDomain;
import ai.libs.hasco.model.CategoricalParameterDomain;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.NumericParameterDomain;
import ai.libs.hasco.model.Parameter;
import ai.libs.hyperopt.optimizer.pcs.PCSConstants;
import ai.libs.hyperopt.util.HASCORepository;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.sets.PartialOrderedSet;

/**
 * For converting PCS format to HASCO format
 *
 * @author kadirayk
 *
 */
public class PCSToHASCOConverter {
	private static final Logger logger = LoggerFactory.getLogger(PCSToHASCOConverter.class);

	private static List<Component> componentsFromRequiredInterfaces = new ArrayList<>();
	private static List<String> componentsFromConditionals = new ArrayList<>();

	/**
	 *
	 * @param repositoryName name of hasco file and repository
	 * @param pcsFolder      path to the folder that contains pcs file
	 */
	public static void generateHASCOFile(final String repositoryName, final String pcsFolder) {
		File[] files = getPCSFiles(pcsFolder);
		List<Component> components = new ArrayList<>();
		for (File pcsFile : files) {
			Component component = convertToHASCOComponent(pcsFile);
			components.add(component);
		}

		for (Component comp : componentsFromRequiredInterfaces) {
			PartialOrderedSet<Parameter> params = comp.getParameters();
			for (Parameter param : params) {
				for (Component component : components) {
					component.getParameters().remove(param);
				}
			}
		}
		components.addAll(componentsFromRequiredInterfaces);
		List<Component> withoutDuplicates = components.stream().distinct().collect(Collectors.toList());

		HASCORepository repository = new HASCORepository();
		repository.setRepository(repositoryName);
		repository.setComponents(withoutDuplicates);
		String jsonString = repository.toString();
		try {
			FileUtils.writeStringToFile(new File("." + File.separator + pcsFolder + File.separator + repositoryName + ".json"), jsonString, "ISO-8859-1");
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private static Component convertToHASCOComponent(final File pcsFile) {
		String componentName = getFullComponentName(pcsFile);
		Component component = new Component(componentName);
		List<String> lines = null;
		try {
			lines = FileUtil.readFileAsList(pcsFile.getAbsolutePath());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		lines = handleComments(lines);
		List<String> conditionals = separateParamsFromConditionals(lines);
		List<String> forbidden = separateForbiddenParams(lines);
		componentsFromConditionals.addAll(conditionals);
		for (String line : lines) {
			Parameter param = null;
			Matcher matcher = PCSConstants.CATEGORICAL_PATTERN.matcher(line);
			if (isInterface(matcher)) {
				handleRequiredInterface(component, line, lines);
			} else {
				if (PCSConstants.NUMERIC_PATTERN.matcher(line).matches()) {
					param = handleNumeric(PCSConstants.NUMERIC_PATTERN.matcher(line));
				} else if (PCSConstants.CATEGORICAL_PATTERN.matcher(line).matches()) {
					param = handleCategorical(PCSConstants.CATEGORICAL_PATTERN.matcher(line));
				}
			}
			if (param != null) {
				component.addParameter(param);
			}
		}

		return component;
	}

	private static boolean isInterface(final Matcher matcher) {
		boolean isInterface = false;
		if (matcher.matches()) {
			String paramName = matcher.group(1).trim();
			isInterface = componentsFromConditionals.stream().anyMatch(c -> (c.contains(paramName + " in") || c.contains(paramName + "in")));
		}
		return isInterface;
	}

	private static Parameter handleCategorical(final Matcher matcher) {
		if (matcher.matches()) {
			String paramName = matcher.group(1).trim();
			String[] values = matcher.group(2).split(",");
			for (int i = 0; i < values.length; i++) {
				values[i] = values[i].trim();
			}
			String defaultValue = matcher.group(3).trim();
			Parameter param = null;
			if (values.length == 2 && ((values[0].equalsIgnoreCase("false") && values[1].equalsIgnoreCase("true")) || (values[0].equalsIgnoreCase("true") && values[1].equalsIgnoreCase("false")))) {
				BooleanParameterDomain domain = new BooleanParameterDomain();
				param = new Parameter(paramName, domain, defaultValue);
			} else {
				CategoricalParameterDomain domain = new CategoricalParameterDomain(values);
				param = new Parameter(paramName, domain, defaultValue);
			}
			return param;
		}
		return null;
	}

	private static Parameter handleNumeric(final Matcher matcher) {
		if (matcher.matches()) {
			String paramName = matcher.group(1).trim();
			String min = matcher.group(3).trim();
			String max = matcher.group(4).trim();
			String defaultValue = matcher.group(5).trim();
			String il = matcher.group(6).trim();
			boolean isInteger = il.equalsIgnoreCase("i");
			NumericParameterDomain domain = null;
			try {
				domain = new NumericParameterDomain(isInteger, Double.valueOf(min).longValue(), Double.parseDouble(max));
			} catch (Exception e) {
				System.out.println(e.getLocalizedMessage());
			}
			return new Parameter(paramName, domain, Double.parseDouble(defaultValue));
		}
		return null;
	}

	private static List<String> separateForbiddenParams(final List<String> lines) {
		List<String> forbiddens = new ArrayList<>();
		for (String line : lines) {
			if (line.startsWith("{") && line.endsWith("}")) {
				forbiddens.add(line);
			}
		}
		lines.removeAll(forbiddens);
		return forbiddens;
	}

	private static List<String> handleComments(final List<String> lines) {
		List<String> cleanLines = new ArrayList<>();
		for (String line : lines) {
			int pos = line.indexOf("#");
			if (pos > 0) {
				cleanLines.add(line.substring(0, pos));
			} else {
				cleanLines.add(line);
			}
		}
		return cleanLines;
	}

	/**
	 * removes conditionals from the given list and returns the conditionals
	 *
	 * @param lines
	 * @return
	 */
	private static List<String> separateParamsFromConditionals(final List<String> lines) {
		List<String> conditionals = new ArrayList<>();
		List<String> otherLinesToRemove = new ArrayList<>();
		for (String line : lines) {
			if (line.trim().isEmpty()) {
				otherLinesToRemove.add(line);
			}
			if (line.contains("Conditionals:")) {
				otherLinesToRemove.add(line);
				continue;
			}
			if (line.contains("|")) {
				conditionals.add(line);
			}
		}
		lines.removeAll(conditionals);
		lines.removeAll(otherLinesToRemove);
		return conditionals;
	}

	private static void handleRequiredInterface(final Component component, final String line, final List<String> parameterLines) {
		String interfaceName = line.split(" ")[0];
		component.addRequiredInterface(interfaceName, interfaceName);
		Matcher matcher = PCSConstants.CATEGORICAL_PATTERN.matcher(line);
		if (matcher.matches()) {
			String interfaces = matcher.group(2);
			interfaces = interfaces.replace(" ", "");
			interfaces = interfaces.replace("{", "").replace("}", "");
			String[] interfaceArr = interfaces.split(",");
			for (String inf : interfaceArr) {
				Component comp = new Component(inf);
				comp.addProvidedInterface(interfaceName);
				componentsFromRequiredInterfaces.add(comp);
				List<String> conditions = componentsFromConditionals.stream().filter(c -> c.contains(" in {" + comp.getName() + "}")).collect(Collectors.toList());
				List<String> otherConditions = componentsFromConditionals.stream().filter(c -> c.contains("| " + comp.getName())).collect(Collectors.toList());
				conditions.addAll(otherConditions);
				for (String cond : conditions) {
					String firstPart = cond.split(" ")[0];
					String paramName = firstPart.split("\\|")[0];
					Optional<String> opt = parameterLines.stream().filter(l -> l.startsWith(paramName)).findFirst();
					if (opt.isPresent()) {
						String paramLine = opt.get();

						Parameter param = null;
						Matcher interfaceMatcher = PCSConstants.CATEGORICAL_PATTERN.matcher(paramLine);
						if (isInterface(interfaceMatcher)) {
							handleRequiredInterface(component, paramLine, parameterLines);
						} else {
							if (PCSConstants.NUMERIC_PATTERN.matcher(paramLine).matches()) {
								param = handleNumeric(PCSConstants.NUMERIC_PATTERN.matcher(paramLine));
							} else if (PCSConstants.CATEGORICAL_PATTERN.matcher(paramLine).matches()) {
								param = handleCategorical(PCSConstants.CATEGORICAL_PATTERN.matcher(paramLine));
							}
						}
						if (param != null) {
							comp.addParameter(param);
						}
					}
				}
			}
		}
	}

	private static String getFullComponentName(final File pcsFile) {
		String fullName = null;
		try {
			List<String> lines = FileUtil.readFileAsList(pcsFile.getAbsolutePath());
			if (lines.get(0).contains("Conditionals")) {
				fullName = pcsFile.getName().replace(".pcs", "");
			} else {
				fullName = lines.get(0).split(" ")[0];
				int lastDot = fullName.lastIndexOf(".");
				if (lastDot == -1) {
					// required interface
					int i = 1;
					while (i < lines.size()) {
						String[] lineElements = lines.get(i).split(" ");
						if (lineElements.length > 1) {
							fullName = lineElements[0];
							lastDot = fullName.lastIndexOf(".");
							if (lastDot != -1) {
								fullName = fullName.substring(0, lastDot);
								break;
							}
						}
						i++;
					}
					if (i == lines.size()) {
						fullName = pcsFile.getName().replace(".pcs", "");
					}
				} else {
					fullName = fullName.substring(0, lastDot);
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return fullName;
	}

	private static Parameter handleNumericParameter(final String[] splitted) {
		String paramName = getSimpleParameterName(splitted[0]);
		String range = splitted[1];
		String defaultVal = splitted[2];
		defaultVal = defaultVal.replace("[", "").replace("]", "");
		String[] valRange = range.replace("[", "").replace("]", "").split(",");
		boolean isInteger = false;
		try {
			Integer.parseInt(valRange[0]);
			isInteger = true;
		} catch (Exception e) {
			isInteger = false;
		}
		Double min = 0.0;
		try {
			min = Double.parseDouble(valRange[0]);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(splitted[0] + splitted[1]);
		}
		Double max = 0.0;
		try {
			max = Double.parseDouble(valRange[1]);
		} catch (Exception e) {
			System.out.println(e);
		}
		NumericParameterDomain domain = new NumericParameterDomain(isInteger, min, max);
		Double defaultDouble = Double.parseDouble(defaultVal);
		Parameter param = new Parameter(paramName, domain, defaultDouble);
		return param;
	}

	private static String getSimpleParameterName(final String fullParamName) {
		int lastDot = fullParamName.lastIndexOf(".");
		return fullParamName.substring(lastDot + 1);
	}

	private static Parameter handleCategoricalParameter(final String[] splitted) {
		String paramName = getSimpleParameterName(splitted[0]);
		String val = splitted[1];
		int bracketIndex = val.indexOf("[");
		String exactValues = val.substring(0, bracketIndex);
		String defaultValue = val.substring(bracketIndex);
		defaultValue = defaultValue.replace("[", "").replace("]", "");
		String[] values = exactValues.replace("{", "").replace("}", "").split(",");
		Parameter param = null;
		if (values[0].equals("true") || values[0].equals("false")) {
			BooleanParameterDomain domain = new BooleanParameterDomain();
			param = new Parameter(paramName, domain, defaultValue);
		} else {
			CategoricalParameterDomain domain = new CategoricalParameterDomain(values);
			param = new Parameter(paramName, domain, defaultValue);
		}

		return param;

	}

	private static File[] getPCSFiles(final String pcsFolder) {
		File dir = new File(pcsFolder);
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return name.endsWith(".pcs");
			}
		});
		return files;
	}

	public static void main(final String[] args) {
		// generateHASCOFile("Auto-WEKA",
		// "PCSBasedOptimizerScripts/HyperBandOptimizer");
		Pattern p = Pattern.compile("([a-zA-Z0-9_\\-@\\.:;\\\\\\/?!$%&*+<>]*) *(\\[-?[0-9]*e?-?[0-9]*\\.?[0-9]*e?-?[0-9]*, *-?[0-9]*\\.?[0-9]*]) *(\\[[0-9]*\\.?[0-9]*])(i?l?)");
		Matcher matcher = p.matcher("extra_trees:max_features [0.01, 1.0] [1.0]");
		matcher.matches();
		matcher.group();
	}

}
