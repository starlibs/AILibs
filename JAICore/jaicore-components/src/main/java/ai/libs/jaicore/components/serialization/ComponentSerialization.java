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
import java.util.Objects;
import java.util.Set;
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
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.api.IParameter;
import ai.libs.jaicore.components.api.IParameterDomain;
import ai.libs.jaicore.components.model.BooleanParameterDomain;
import ai.libs.jaicore.components.model.CategoricalParameterDomain;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentRepository;
import ai.libs.jaicore.components.model.Dependency;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.model.Parameter;
import ai.libs.jaicore.components.model.ParameterRefinementConfiguration;

public class ComponentSerialization implements ILoggingCustomizable {

	private static final String STR_VALUES = "values";
	private static final String STR_DEFAULT = "default";
	private static final String MSG_CANNOT_PARSE_LITERAL = "Cannot parse literal ";
	private static final String MSG_DOMAIN_NOT_SUPPORTED = "Currently no support for parameters with domain \"";

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
		on.put("component", instance.getComponent().getName());
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
		return this.readRepositoryFile(jsonFile, new ArrayList<>(), new HashMap<>());
	}

	private JsonNode readRepositoryFile(final File jsonFile, final List<String> parsedFiles, final Map<String, JsonNode> parameterMap) throws IOException {
		this.logger.debug("Parse file {}...", jsonFile.getAbsolutePath());

		String jsonDescription;
		if (jsonFile instanceof ResourceFile) {
			jsonDescription = ResourceUtil.readResourceFileToString(((ResourceFile) jsonFile).getPathName());
		} else {
			jsonDescription = FileUtil.readFileAsString(jsonFile);
		}
		jsonDescription = jsonDescription.replaceAll("/\\*(.*)\\*/", "");

		ObjectMapper om = new ObjectMapper();
		JsonNode rootNode = om.readTree(jsonDescription);

		for (JsonNode elem : rootNode.path("parameters")) {
			parameterMap.put(elem.get("name").asText(), elem);
		}
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
				this.readRepositoryFile(subFile);
			}
		}
		return rootNode;
	}

	public IComponentRepository deserializeRepository(final File jsonFile) throws IOException {
		return this.deserializeRepository(this.readRepositoryFile(jsonFile));
	}

	public IComponentRepository deserializeRepository(final String repository) throws IOException {
		return this.deserializeRepository(new ObjectMapper().readTree(repository));
	}

	public Map<IComponent, Map<IParameter, ParameterRefinementConfiguration>> deserializeParamMap(final File jsonFile) throws IOException {
		return this.deserializeParamMap(this.readRepositoryFile(jsonFile));
	}

	public Map<IComponent, Map<IParameter, ParameterRefinementConfiguration>> deserializeParamMap(final String json) throws IOException {
		return this.deserializeParamMap(new ObjectMapper().readTree(json));
	}

	public Map<IComponent, Map<IParameter, ParameterRefinementConfiguration>> deserializeParamMap(final JsonNode rootNode) {
		return this.deserializeRepositoryAndParamConfig(rootNode).getY();
	}

	public IComponentRepository deserializeRepository(final JsonNode rootNode) {
		return this.deserializeRepositoryAndParamConfig(rootNode).getX();
	}

	public Pair<IComponentRepository, Map<IComponent, Map<IParameter, ParameterRefinementConfiguration>>> deserializeRepositoryAndParamConfig(final JsonNode rootNode) {

		Map<IComponent, Map<IParameter, ParameterRefinementConfiguration>> paramConfigs = new HashMap<>();
		Map<String, JsonNode> parameterMap = new HashMap<>();
		Set<String> uniqueComponentNames = new HashSet<>();

		Map<String, JsonNode> componentMap = new HashMap<>();

		/* create empty repository */
		ComponentRepository repository = new ComponentRepository();

		// get the array of components
		JsonNode describedComponents = rootNode.path("components");
		if (describedComponents != null) {
			Component c;
			for (JsonNode component : describedComponents) {
				c = new Component(component.get("name").asText());
				if (c.getName().contains("-")) {
					throw new IllegalArgumentException("Illegal component name " + c.getName() + ". No hyphens allowed. Please only use [a-zA-z0-9].");
				}
				componentMap.put(c.getName(), component);

				if (!uniqueComponentNames.add(c.getName())) {
					throw new IllegalArgumentException("Noticed a component with duplicative component name: " + c.getName());
				}

				// add provided interfaces

				for (JsonNode providedInterface : component.path("providedInterface")) {
					c.addProvidedInterface(providedInterface.asText());
				}

				// add required interfaces
				for (JsonNode requiredInterface : component.path("requiredInterface")) {
					if (!requiredInterface.has("id")) {
						throw new IllegalArgumentException("No id has been specified for a required interface of " + c.getName());
					}
					if (!requiredInterface.has("name")) {
						throw new IllegalArgumentException("No name has been specified for a required interface of " + c.getName());
					}
					// BEWARE: any changes here must also be reflected on the Interface.java @JsonCreator so that
					if (requiredInterface.has("optional")) {
						if (!requiredInterface.has("min") && !requiredInterface.has("max")) {
							if (requiredInterface.get("optional").asBoolean()) {
								c.addRequiredInterface(requiredInterface.get("id").asText(),
										requiredInterface.get("name").asText(),
										0,
										1);
							} else {
								c.addRequiredInterface(requiredInterface.get("id").asText(),
										requiredInterface.get("name").asText(),
										1,
										1);
							}
						} else {
							throw new IllegalArgumentException("When specifying \"optional\" for a required interface, both\"min\" and \"max\" must be omitted");
						}
					} else { // optional is missing
						if (!requiredInterface.has("min") && !requiredInterface.has("max")) {
							c.addRequiredInterface(requiredInterface.get("id").asText(),
									requiredInterface.get("name").asText(),
									1,
									1);
						}
						else if (requiredInterface.has("min") && requiredInterface.has("max")) {
							int min = requiredInterface.get("min").asInt();
							int max = requiredInterface.get("max").asInt();
							if (min <= max) {
								c.addRequiredInterface(requiredInterface.get("id").asText(),
										requiredInterface.get("name").asText(),
										requiredInterface.get("min").asInt(),
										requiredInterface.get("max").asInt());
							} else {
								throw new IllegalArgumentException("When declaring a required interface, \"min\" should be lesser than \"max\"");
							}
						} else {
							throw new IllegalArgumentException("If not specifying \"optional\" for a required interface, either both \"min\" and \"max\" must be specified or none at all");
						}
					}
				}

				Map<IParameter, ParameterRefinementConfiguration> paramConfig = new HashMap<>();

				for (JsonNode parameter : component.path("parameter")) {
					// name of the parameter
					String name = parameter.get("name").asText();
					// possible string params
					String[] stringParams = new String[] { "type", STR_VALUES, STR_DEFAULT };
					String[] stringParamValues = new String[stringParams.length];
					// possible boolean params
					String[] boolParams = new String[] { STR_DEFAULT, "includeExtremals" };
					boolean[] boolParamValues = new boolean[boolParams.length];
					// possible double params
					String[] doubleParams = new String[] { STR_DEFAULT, "min", "max", "refineSplits", "minInterval" };
					double[] doubleParamValues = new double[doubleParams.length];

					if (parameterMap.containsKey(name)) {
						JsonNode commonParameter = parameterMap.get(name);
						// get string parameter values from common parameter
						for (int i = 0; i < stringParams.length; i++) {
							if (commonParameter.get(stringParams[i]) != null) {
								stringParamValues[i] = commonParameter.get(stringParams[i]).asText();
							}
						}
						// get double parameter values from common parameter
						for (int i = 0; i < doubleParams.length; i++) {
							if (commonParameter.get(doubleParams[i]) != null) {
								doubleParamValues[i] = commonParameter.get(doubleParams[i]).asDouble();
							}
						}
						// get boolean parameter values from common parameter
						for (int i = 0; i < boolParams.length; i++) {
							if (commonParameter.get(boolParams[i]) != null) {
								boolParamValues[i] = commonParameter.get(boolParams[i]).asBoolean();
							}
						}
					}

					// get string parameter values from current parameter
					for (int i = 0; i < stringParams.length; i++) {
						if (parameter.get(stringParams[i]) != null) {
							stringParamValues[i] = parameter.get(stringParams[i]).asText();
						}
					}
					// get double parameter values from current parameter
					for (int i = 0; i < doubleParams.length; i++) {
						if (parameter.get(doubleParams[i]) != null) {
							doubleParamValues[i] = parameter.get(doubleParams[i]).asDouble();
						}
					}
					// get boolean parameter values from current parameter
					for (int i = 0; i < boolParams.length; i++) {
						if (parameter.get(boolParams[i]) != null) {
							boolParamValues[i] = parameter.get(boolParams[i]).asBoolean();
						}
					}

					Parameter p = null;
					String type = stringParamValues[Arrays.stream(stringParams).collect(Collectors.toList()).indexOf("type")];
					switch (type) {
					case "int":
					case "int-log":
					case "double":
					case "double-log":
						p = new Parameter(name, new NumericParameterDomain(type.equals("int") || type.equals("int-log"), doubleParamValues[1], doubleParamValues[2]), doubleParamValues[0]);
						if (doubleParamValues[3] == 0) {
							throw new IllegalArgumentException("Please specify the parameter \"refineSplits\" for the parameter \"" + p.getName() + "\" in component \"" + c.getName() + "\"");
						}
						if (doubleParamValues[4] <= 0) {
							throw new IllegalArgumentException("Please specify a strictly positive parameter value for \"minInterval\" for the parameter \"" + p.getName() + "\" in component \"" + c.getName() + "\"");
						}
						if (type.endsWith("-log")) {
							paramConfig.put(p, new ParameterRefinementConfiguration(parameter.get("focus").asDouble(), parameter.get("basis").asDouble(), boolParamValues[1], (int) doubleParamValues[3], doubleParamValues[4]));

						} else {
							paramConfig.put(p, new ParameterRefinementConfiguration(boolParamValues[1], (int) doubleParamValues[3], doubleParamValues[4]));
						}
						break;
					case "bool":
					case "boolean":
						p = new Parameter(name, new BooleanParameterDomain(), boolParamValues[0]);
						break;
					case "cat":
						if (parameter.get(STR_VALUES) != null && parameter.get(STR_VALUES).isTextual()) {
							p = new Parameter(name, new CategoricalParameterDomain(Arrays.stream(stringParamValues[1].split(",")).collect(Collectors.toList())), stringParamValues[2]);
						} else {
							List<String> values = new LinkedList<>();

							if (parameter.get(STR_VALUES) != null) {
								for (JsonNode value : parameter.get(STR_VALUES)) {
									values.add(value.asText());
								}
							} else if (parameterMap.containsKey(name)) {
								for (JsonNode value : parameterMap.get(name).get(STR_VALUES)) {
									values.add(value.asText());
								}
							} else {
								this.logger.error("Warning: Categorical parameter {} in component {} without value list.", name, c.getName());
							}
							try {
								p = new Parameter(name, new CategoricalParameterDomain(values), stringParamValues[2]);
							}
							catch (Exception e) {
								throw new IllegalArgumentException("Error in parsing definition of component " + c.getName() + ".", e);
							}
						}
						break;
					default:
						throw new IllegalArgumentException("Unsupported parameter type " + type);
					}
					if (p != null) {
						c.addParameter(p);
					}
				}

				/* now parse dependencies */
				for (JsonNode dependency : component.path("dependencies")) {

					/* parse precondition */
					String pre = dependency.get("pre").asText();
					Collection<Collection<Pair<IParameter, IParameterDomain>>> premise = new ArrayList<>();
					Collection<String> monoms = Arrays.asList(pre.split("\\|"));
					for (String monom : monoms) {
						Collection<String> literals = Arrays.asList(monom.split("&"));
						Collection<Pair<IParameter, IParameterDomain>> monomInPremise = new ArrayList<>();

						for (String literal : literals) {
							String[] parts = literal.trim().split(" ");
							if (parts.length != 3) {
								throw new IllegalArgumentException(MSG_CANNOT_PARSE_LITERAL + literal + ". Literals must be of the form \"<a> P <b>\".");
							}

							IParameter param = c.getParameterWithName(parts[0]);
							String target = parts[2];
							switch (parts[1]) {
							case "=":
								Pair<IParameter, IParameterDomain> eqConditionItem;
								if (param.isNumeric()) {
									double val = Double.parseDouble(target);
									eqConditionItem = new Pair<>(param, new NumericParameterDomain(((NumericParameterDomain) param.getDefaultDomain()).isInteger(), val, val));
								} else if (param.isCategorical()) {
									eqConditionItem = new Pair<>(param, new CategoricalParameterDomain(new String[] { target }));
								} else {
									throw new IllegalArgumentException(MSG_DOMAIN_NOT_SUPPORTED+ param.getDefaultDomain().getClass().getName() + "\"");
								}
								monomInPremise.add(eqConditionItem);
								break;

							case "in":
								Pair<IParameter, IParameterDomain> inConditionItem;
								if (param.isNumeric()) {
									Interval interval = SetUtil.unserializeInterval("[" + target.substring(1, target.length() - 1) + "]");
									inConditionItem = new Pair<>(param, new NumericParameterDomain(((NumericParameterDomain) param.getDefaultDomain()).isInteger(), interval.getInf(), interval.getSup()));
								} else if (param.isCategorical()) {
									if (!target.startsWith("[") && !target.startsWith("{")) {
										throw new IllegalArgumentException("Illegal literal \"" + literal + "\" in the postcondition of dependency. This should be a set, but the target is not described by [...] or {...}");
									}
									Collection<String> values = target.startsWith("[") ? SetUtil.unserializeList(target) : SetUtil.unserializeSet(target);
									inConditionItem = new Pair<>(param, new CategoricalParameterDomain(values));
								} else {
									throw new IllegalArgumentException(MSG_DOMAIN_NOT_SUPPORTED + param.getDefaultDomain().getClass().getName() + "\"");
								}
								monomInPremise.add(inConditionItem);
								break;
							default:
								throw new IllegalArgumentException(MSG_CANNOT_PARSE_LITERAL + literal + ". Currently no support for predicate \"" + parts[1] + "\".");
							}
						}
						premise.add(monomInPremise);
					}

					/* parse postcondition */
					Collection<Pair<IParameter, IParameterDomain>> conclusion = new ArrayList<>();
					String post = dependency.get("post").asText();
					Collection<String> literals = Arrays.asList(post.split("&"));

					for (String literal : literals) {
						String[] parts = literal.trim().split(" ");
						if (parts.length < 3) {
							throw new IllegalArgumentException(MSG_CANNOT_PARSE_LITERAL + literal + ". Literals must be of the form \"<a> P <b>\".");
						}
						if (parts.length > 3) {
							for (int i = 3; i < parts.length; i++) {
								parts[2] += " " + parts[i];
							}
						}

						IParameter param = c.getParameterWithName(parts[0]);
						String target = parts[2];
						switch (parts[1]) {
						case "=":
							Pair<IParameter, IParameterDomain> eqConditionItem;
							if (param.isNumeric()) {
								double val = Double.parseDouble(target);
								eqConditionItem = new Pair<>(param, new NumericParameterDomain(((NumericParameterDomain) param.getDefaultDomain()).isInteger(), val, val));
							} else if (param.isCategorical()) {
								eqConditionItem = new Pair<>(param, new CategoricalParameterDomain(new String[] { target }));
							} else {
								throw new IllegalArgumentException(MSG_DOMAIN_NOT_SUPPORTED + param.getDefaultDomain().getClass().getName() + "\"");
							}
							conclusion.add(eqConditionItem);
							break;

						case "in":
							Pair<IParameter, IParameterDomain> inConditionItem;
							if (param.isNumeric()) {
								Interval interval = SetUtil.unserializeInterval("[" + target.substring(1, target.length() - 1) + "]");
								inConditionItem = new Pair<>(param, new NumericParameterDomain(((NumericParameterDomain) param.getDefaultDomain()).isInteger(), interval.getInf(), interval.getSup()));
							} else if (param.isCategorical()) {
								if (!target.startsWith("[") && !target.startsWith("{")) {
									throw new IllegalArgumentException("Illegal literal \"" + literal + "\" in the postcondition of dependency. This should be a set, but the target is not described by [...] or {...}");
								}
								Collection<String> values = target.startsWith("[") ? SetUtil.unserializeList(target) : SetUtil.unserializeSet(target);
								inConditionItem = new Pair<>(param, new CategoricalParameterDomain(values));
							} else {
								throw new IllegalArgumentException(MSG_DOMAIN_NOT_SUPPORTED + param.getDefaultDomain().getClass().getName() + "\"");
							}
							conclusion.add(inConditionItem);
							break;
						default:
							throw new IllegalArgumentException(MSG_CANNOT_PARSE_LITERAL + literal + ". Currently no support for predicate \"" + parts[1] + "\".");
						}
					}
					/* add dependency to the component */
					c.addDependency(new Dependency(premise, conclusion));
				}

				paramConfigs.put(c, paramConfig);
				repository.add(c);
			}
		}
		return new Pair<>(repository, paramConfigs);
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
