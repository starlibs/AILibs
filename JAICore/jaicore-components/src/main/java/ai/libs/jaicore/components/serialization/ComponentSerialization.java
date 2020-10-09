package ai.libs.jaicore.components.serialization;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.ResourceFile;
import ai.libs.jaicore.basic.ResourceUtil;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IComponentInstanceConstraint;
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.api.INumericParameterRefinementConfigurationMap;
import ai.libs.jaicore.components.api.IParameter;
import ai.libs.jaicore.components.api.IParameterDependency;
import ai.libs.jaicore.components.api.IParameterDomain;
import ai.libs.jaicore.components.api.IRequiredInterfaceDefinition;
import ai.libs.jaicore.components.model.BooleanParameterDomain;
import ai.libs.jaicore.components.model.CategoricalParameterDomain;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstanceConstraint;
import ai.libs.jaicore.components.model.ComponentRepository;
import ai.libs.jaicore.components.model.Dependency;
import ai.libs.jaicore.components.model.Interface;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.model.NumericParameterRefinementConfiguration;
import ai.libs.jaicore.components.model.NumericParameterRefinementConfigurationMap;
import ai.libs.jaicore.components.model.Parameter;

public class ComponentSerialization implements ILoggingCustomizable {

	public static final String FIELD_COMPONENTS = "components";
	public static final String FIELD_CONSTRAINTS = "constraints";
	public static final String FIELD_PARAMETERS = "parameters";

	public static final String DTYPE_DOUBLE = "double";
	public static final String DTYPE_INT = "int";

	private static final String FIELD_DEFAULT = "default";
	private static final String MSG_CANNOT_PARSE_LITERAL = "Cannot parse literal ";

	private static final Pattern PATTERN_DEPENDENCY = Pattern.compile("(\\S+)\\s*(?:(=)(.*)|(in) (\\[.*\\]|\\{.*\\}))");

	private Logger logger = LoggerFactory.getLogger(ComponentSerialization.class);

	public ComponentSerialization() {

	}

	public ComponentSerialization(final String loggerName) {
		this();
		this.setLoggerName(loggerName);
	}

	public JsonNode serialize(final IComponentInstance instance) {
		Objects.requireNonNull(instance);
		ObjectMapper om = new ObjectMapper();
		ObjectNode on = om.createObjectNode();

		/* define component and params */
		on.put(FIELD_COMPONENTS, instance.getComponent().getName());
		ObjectNode params = om.createObjectNode();
		for (String paramName : instance.getParameterValues().keySet()) {
			params.put(paramName, instance.getParameterValues().get(paramName));
		}
		on.set("params", params);

		/* define how required interfaces have been resolved */
		ObjectNode requiredInterfaces = om.createObjectNode();
		for (String requiredInterface : instance.getSatisfactionOfRequiredInterfaces().keySet()) {
			ArrayNode componentInstancesHere = om.createArrayNode();
			instance.getSatisfactionOfRequiredInterfaces().get(requiredInterface).forEach(ci -> componentInstancesHere.add(this.serialize(ci)));
			requiredInterfaces.set(requiredInterface, componentInstancesHere);
		}
		on.set("requiredInterfaces", requiredInterfaces);

		return on;
	}

	public JsonNode readRepositoryFile(final File jsonFile) throws IOException {
		return this.readRepositoryFile(jsonFile, new HashMap<>());
	}

	public JsonNode readRepositoryFile(final File jsonFile, final Map<String, String> templateVars) throws IOException {
		return this.readRepositoryFile(jsonFile, templateVars, new ArrayList<>());
	}

	private JsonNode readRepositoryFile(final File jsonFile, final Map<String, String> templateVars, final List<String> parsedFiles) throws IOException {

		/* read in file as string */
		this.logger.info("Parse file {} with environment variables: {}", jsonFile.getAbsolutePath(), templateVars);
		String jsonDescription;
		if (jsonFile instanceof ResourceFile) {
			jsonDescription = ResourceUtil.readResourceFileToString(((ResourceFile) jsonFile).getPathName());
		} else {
			jsonDescription = FileUtil.readFileAsString(jsonFile);
		}
		jsonDescription = jsonDescription.replaceAll("/\\*(.*)\\*/", "");
		for (Entry<String, String> replacementRule : templateVars.entrySet()) {
			jsonDescription = jsonDescription.replace("{$" + replacementRule.getKey() + "}", replacementRule.getValue());
		}

		/* convert the string into a JsonNode */
		ObjectMapper om = new ObjectMapper();
		JsonNode rootNode = om.readTree(jsonDescription);
		JsonNode compNode = rootNode.get(FIELD_COMPONENTS);
		if (compNode != null && !compNode.isArray()) {
			throw new IllegalArgumentException("Components field in repository file " + jsonFile.getAbsolutePath() + " is not defined or not an array!");
		}
		ArrayNode compNodeAsArray = compNode != null ? (ArrayNode) compNode : om.createArrayNode();
		if (compNode == null) {
			((ObjectNode) rootNode).set(FIELD_COMPONENTS, compNodeAsArray);
		}
		if (this.logger.isInfoEnabled()) {
			compNodeAsArray.forEach(n -> this.logger.info("Adding component {}", n));
		}

		/* if there are includes, resolve them now and attach the components of all of them to the component array */
		JsonNode includes = rootNode.path("include");
		File baseFolder = jsonFile.getParentFile();
		for (JsonNode includePathNode : includes) {
			String path = includePathNode.asText();
			File subFile;
			if (baseFolder instanceof ResourceFile) {
				subFile = new ResourceFile((ResourceFile) baseFolder, path);
			} else {
				subFile = new File(baseFolder, path);
			}

			if (!parsedFiles.contains(subFile.getCanonicalPath())) {
				parsedFiles.add(subFile.getCanonicalPath());
				this.logger.debug("Recursively including component repository from {}.", subFile);
				JsonNode subRepository = this.readRepositoryFile(subFile, templateVars);
				JsonNode compsInSubRepository = subRepository.get(FIELD_COMPONENTS);
				ArrayNode compsInSubRepositoryAsArray = (ArrayNode) compsInSubRepository;
				compNodeAsArray.addAll(compsInSubRepositoryAsArray);

			}
		}
		return rootNode;
	}

	public IComponentRepository deserializeRepository(final File jsonFile) throws IOException {
		return this.deserializeRepository(jsonFile, new HashMap<>());
	}

	public IComponentRepository deserializeRepository(final File jsonFile, final Map<String, String> templateVars) throws IOException {
		try {
			return this.deserializeRepository(this.readRepositoryFile(jsonFile, templateVars));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Found a problem when parsing repository file " + jsonFile, e);
		}
	}

	public IComponentRepository deserializeRepository(final String repository) throws IOException {
		return this.deserializeRepository(new ObjectMapper().readTree(repository));
	}

	public INumericParameterRefinementConfigurationMap deserializeParamMap(final File jsonFile) throws IOException {
		return this.deserializeParamMap(this.readRepositoryFile(jsonFile));
	}

	public INumericParameterRefinementConfigurationMap deserializeParamMap(final String json) throws IOException {
		return this.deserializeParamMap(new ObjectMapper().readTree(json));
	}

	public INumericParameterRefinementConfigurationMap deserializeParamMap(final JsonNode rootNode) {
		return this.deserializeRepositoryAndParamConfig(rootNode).getY();
	}

	public IComponentRepository deserializeRepository(final JsonNode rootNode) {
		return this.deserializeRepositoryAndParamConfig(rootNode).getX();
	}

	private void checkThatParameterDefinitionHasNameAndType(final JsonNode parameter) {
		if (!parameter.has("name")) {
			throw new IllegalArgumentException("No name defined for parameter node \"" + parameter + "\"");
		}
		if (!parameter.has("type")) {
			throw new IllegalArgumentException("No type defined for parameter \"" + parameter.get("name").asText() + "\"");
		}
	}

	public IParameter deserializeParameter(final JsonNode parameter) {
		this.checkThatParameterDefinitionHasNameAndType(parameter);
		String name = parameter.get("name").asText();
		if (!parameter.has(FIELD_DEFAULT)) {
			throw new IllegalArgumentException("No default value defined for parameter \"" + name + "\"");
		}
		JsonNode defValNode = parameter.get(FIELD_DEFAULT);
		String type = parameter.get("type").asText();
		switch (type) {
		case DTYPE_INT:
		case DTYPE_INT + "-log":
		case DTYPE_DOUBLE:
		case DTYPE_DOUBLE + "-log":
			if (!parameter.has("min")) {
				throw new IllegalArgumentException("No min value defined for parameter " + name);
			}
		if (!parameter.has("max")) {
			throw new IllegalArgumentException("No max value defined for parameter " + name);
		}
		double min = parameter.get("min").asDouble();
		double max = parameter.get("max").asDouble();
		return new Parameter(name, new NumericParameterDomain(type.equals("int") || type.equals("int-log"), min, max), defValNode.asDouble());

		case "bool":
		case "boolean":
			return new Parameter(name, new BooleanParameterDomain(), defValNode.asBoolean());

		case "cat":
		case "categoric":
		case "categorical":
			if (!parameter.has("values")) {
				throw new IllegalArgumentException("Categorical parameter \"" + name + "\" has no field \"values\" for the possible values defined!");
			}
			JsonNode valuesNode = parameter.get("values");
			List<String> values = new LinkedList<>();
			if (valuesNode.isTextual()) {
				values.addAll(Arrays.stream(valuesNode.asText().split(",")).collect(Collectors.toList()));
			} else {
				for (JsonNode value : valuesNode) {
					values.add(value.asText());
				}
			}
			return new Parameter(name, new CategoricalParameterDomain(values), defValNode.asText());

		default:
			throw new IllegalArgumentException("Unsupported parameter type " + type);
		}
	}

	public NumericParameterRefinementConfiguration deserializeParamRefinement(final JsonNode parameter) {
		this.checkThatParameterDefinitionHasNameAndType(parameter);
		String name = parameter.get("name").asText();
		String type = parameter.get("type").asText();
		if (!type.startsWith(DTYPE_INT) && !type.startsWith(DTYPE_DOUBLE)) {
			throw new IllegalArgumentException("Parameter type is " + type + " and hence not numeric!");
		}

		if (!parameter.has("refineSplits")) {
			throw new IllegalArgumentException("Please specify the parameter \"refineSplits\" for the parameter \"" + name + "\" in component \"" + name + "\"");
		}
		if (!parameter.has("minInterval")) {
			throw new IllegalArgumentException("Please specify a strictly positive parameter value for \"minInterval\" for the parameter \"" + name + "\" in component \"" + name + "\"");
		}
		boolean initWithExtremal = false;
		int refineSplits = parameter.get("refineSplits").asInt();
		double minInterval = parameter.get("minInterval").asDouble();
		if (type.endsWith("-log")) {
			return new NumericParameterRefinementConfiguration(parameter.get("focus").asDouble(), parameter.get("basis").asDouble(), initWithExtremal, refineSplits, minInterval);
		} else {
			return new NumericParameterRefinementConfiguration(initWithExtremal, refineSplits, minInterval);
		}
	}

	public Pair<IParameter, IParameterDomain> deserializeDependencyConditionTerm(final IComponent component, final String condition) {
		Matcher m = PATTERN_DEPENDENCY.matcher(condition.trim());
		if (!m.find()) {
			throw new IllegalArgumentException(MSG_CANNOT_PARSE_LITERAL + condition.trim() + ". Literals must be of the form \"<a> P <b>\" where P is either '=' or 'in'.");
		}

		String lhs = m.group(1).trim();
		String cond = (m.group(2) != null ? m.group(2) : m.group(4)).trim();
		String rhs = (cond.equals("=") ? m.group(3) : m.group(5)).trim();
		IParameter param = component.getParameter(lhs);
		if (param.isNumeric()) {
			return this.deserializeDependencyConditionTermForNumericalParam(param, lhs, cond, rhs);
		} else if (param.isCategorical()) {
			return this.deserializeDependencyConditionTermForCategoricalParam(param, lhs, cond, rhs);
		} else {
			throw new IllegalArgumentException("Parameter \"" + param.getName() + "\" must be numeric or categorical!");
		}
	}

	public Pair<IParameter, IParameterDomain> deserializeDependencyConditionTermForCategoricalParam(final IParameter param, final String lhs, final String comp, final String rhs) {
		switch (comp) {
		case "=":
			return new Pair<>(param, new CategoricalParameterDomain(new String[] { rhs }));
		case "in":
			if (!rhs.startsWith("[") && !rhs.startsWith("{")) {
				throw new IllegalArgumentException("Illegal literal \"" + lhs + "\" in \"" + rhs + "\". This should be a set, but the target is not described by [...] or {...}");
			}
			Collection<String> values = rhs.startsWith("[") ? SetUtil.unserializeList(rhs) : SetUtil.unserializeSet(rhs);
			return new Pair<>(param, new CategoricalParameterDomain(values));
		default:
			throw new IllegalArgumentException(MSG_CANNOT_PARSE_LITERAL + lhs + " " + comp + " " + rhs + ". Currently no support for predicate \"" + comp + "\".");
		}
	}

	public Pair<IParameter, IParameterDomain> deserializeDependencyConditionTermForNumericalParam(final IParameter param, final String lhs, final String comp, final String rhs) {
		switch (comp) {
		case "=":
			double val = Double.parseDouble(rhs);
			return new Pair<>(param, new NumericParameterDomain(((NumericParameterDomain) param.getDefaultDomain()).isInteger(), val, val));
		case "in":
			Interval interval = SetUtil.unserializeInterval("[" + rhs.substring(1, rhs.length() - 1) + "]");
			return new Pair<>(param, new NumericParameterDomain(((NumericParameterDomain) param.getDefaultDomain()).isInteger(), interval.getInf(), interval.getSup()));
		default:
			throw new IllegalArgumentException(MSG_CANNOT_PARSE_LITERAL + lhs + " " + comp + " " + rhs + ". Currently no support for predicate \"" + comp + "\".");
		}
	}

	public IParameterDependency deserializeParameterDependency(final IComponent c, final JsonNode dependency) {

		/* parse precondition */
		String pre = dependency.get("pre").asText();
		Collection<Collection<Pair<IParameter, IParameterDomain>>> premise = new ArrayList<>();
		Collection<String> monoms = Arrays.asList(pre.split("\\|"));
		for (String monom : monoms) {
			Collection<String> literals = Arrays.asList(monom.split("&"));
			Collection<Pair<IParameter, IParameterDomain>> monomInPremise = new ArrayList<>();
			for (String literal : literals) {
				monomInPremise.add(this.deserializeDependencyConditionTerm(c, literal));
			}
			premise.add(monomInPremise);
		}

		/* parse postcondition */
		Collection<Pair<IParameter, IParameterDomain>> conclusion = new ArrayList<>();
		String post = dependency.get("post").asText();
		Collection<String> literals = Arrays.asList(post.split("&"));
		for (String literal : literals) {
			conclusion.add(this.deserializeDependencyConditionTerm(c, literal));
		}
		return new Dependency(premise, conclusion);
	}

	public IRequiredInterfaceDefinition deserializeRequiredInterface(final JsonNode requiredInterface) {

		/* read in relevant variables for required interface definition */
		if (!requiredInterface.has("id")) {
			throw new IllegalArgumentException("No id has been specified for a required interface " + requiredInterface);
		}
		if (!requiredInterface.has("name")) {
			throw new IllegalArgumentException("No name has been specified for a required interface " + requiredInterface);
		}
		String id = requiredInterface.get("id").asText();
		boolean optional = requiredInterface.has("optional") && requiredInterface.get("optional").asBoolean(); // default is false
		boolean unique = requiredInterface.has("unique") && requiredInterface.get("unique").asBoolean(); // default is false
		boolean ordered = !requiredInterface.has("ordered") || requiredInterface.get("ordered").asBoolean(); // default is true
		int min = requiredInterface.has("min") ? requiredInterface.get("min").asInt() : 1;
		int max = requiredInterface.has("max") ? requiredInterface.get("max").asInt() : min;
		if (min > max) {
			throw new IllegalArgumentException(min + " = min > max = " + max + " for required interface " + requiredInterface);
		}
		return new Interface(id, requiredInterface.get("name").asText(), optional, unique, ordered, min, max);
	}

	public IComponent deserializeComponent(final JsonNode component) {
		Component c = new Component(component.get("name").asText());
		if (c.getName().contains("-")) {
			throw new IllegalArgumentException("Illegal component name " + c.getName() + ". No hyphens allowed. Please only use [a-zA-z0-9].");
		}

		// add provided interfaces
		for (JsonNode providedInterface : component.path("providedInterface")) {
			c.addProvidedInterface(providedInterface.asText());
		}

		// add required interfaces
		for (JsonNode requiredInterface : component.path("requiredInterface")) {
			try {
				c.addRequiredInterface(this.deserializeRequiredInterface(requiredInterface));
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Error when parsing required interface of component \"" + c.getName() + "\"", e);
			}
		}

		/* attach parameters */
		for (JsonNode parameter : component.get(FIELD_PARAMETERS)) {
			c.addParameter(this.deserializeParameter(parameter));
		}

		/* now parse dependencies */
		if (component.has("dependencies")) {
			for (JsonNode dependency : component.get("dependencies")) {
				c.addDependency(this.deserializeParameterDependency(c, dependency));
			}
		}

		return c;
	}

	public INumericParameterRefinementConfigurationMap deserializeParamRefinementConfiguration(final JsonNode component) {
		NumericParameterRefinementConfigurationMap paramConfigs = new NumericParameterRefinementConfigurationMap();
		Map<String, NumericParameterRefinementConfiguration> map = new HashMap<>();
		String componentName = component.get("name").asText();
		paramConfigs.put(componentName, map);
		for (JsonNode parameter : component.get(FIELD_PARAMETERS)) {
			String paramName = parameter.get("name").asText();
			String type = parameter.get("type").asText();
			if (type.startsWith(DTYPE_INT) || type.startsWith(DTYPE_DOUBLE)) {
				try {
					map.put(paramName, this.deserializeParamRefinement(parameter));
				} catch (RuntimeException e) {
					throw new IllegalArgumentException("Observed problems when processing parameter " + paramName + " of component " + componentName);
				}
			}
		}
		return paramConfigs;
	}

	public IComponentInstanceConstraint deserializeConstraint(final Collection<IComponent> components, final JsonNode constraint) {
		boolean positive = !constraint.has("positive") || constraint.get("positive").asBoolean();
		if (!constraint.has("premise")) {
			throw new IllegalArgumentException("Constraint has no premise: " + constraint);
		}
		if (!constraint.has("conclusion")) {
			throw new IllegalArgumentException("Constraint has no conclusion: " + constraint);
		}
		try {
			IComponentInstance premise = new ComponentInstanceDeserializer(components).readAsTree(constraint.get("premise"));

			IComponentInstance conclusion = new ComponentInstanceDeserializer(components).readAsTree(constraint.get("conclusion"));
			return new ComponentInstanceConstraint(positive, premise, conclusion);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public Pair<IComponentRepository, INumericParameterRefinementConfigurationMap> deserializeRepositoryAndParamConfig(final JsonNode rootNode) {

		NumericParameterRefinementConfigurationMap paramConfigs = new NumericParameterRefinementConfigurationMap();
		Set<String> uniqueComponentNames = new HashSet<>();

		Map<String, JsonNode> componentMap = new HashMap<>();

		/* create repository with components */
		Collection<IComponent> components = new ArrayList<>();
		JsonNode describedComponents = rootNode.path(FIELD_COMPONENTS);
		if (describedComponents != null) {
			for (JsonNode component : describedComponents) {

				/* derive component */
				IComponent c = this.deserializeComponent(component);
				componentMap.put(c.getName(), component);
				if (!uniqueComponentNames.add(c.getName())) {
					throw new IllegalArgumentException("Noticed a component with duplicative component name: " + c.getName());
				}
				components.add(c);

				/* derive parameter refinement description */
				Map<String, NumericParameterRefinementConfiguration> paramConfig = ((NumericParameterRefinementConfigurationMap) this.deserializeParamRefinementConfiguration(component)).get(c.getName());
				paramConfigs.put(c.getName(), paramConfig);
			}
		}

		/* now parse constraints */
		Collection<IComponentInstanceConstraint> constraints = new ArrayList<>();
		if (rootNode.has(FIELD_CONSTRAINTS)) {
			for (JsonNode constraint : rootNode.get(FIELD_CONSTRAINTS)) {
				constraints.add(this.deserializeConstraint(components, constraint));
			}
		}

		return new Pair<>(new ComponentRepository(components, constraints), paramConfigs);
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}
