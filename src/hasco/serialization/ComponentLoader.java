package hasco.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import hasco.model.Component;
import hasco.model.NumericParameter;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;

public class ComponentLoader {

  private Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramConfigs = new HashMap<>();
  private Collection<Component> components = new ArrayList<>();

  public ComponentLoader() {

  }

  public void loadComponents(final File commonParametersFile, final File componentDescriptionFile) throws IOException {

    ObjectMapper objectMapper = new ObjectMapper();
    byte[] commonParametersData = Files.readAllBytes(commonParametersFile.toPath());
    JsonNode paramRoot = objectMapper.readTree(commonParametersFile);

    Map<String, JsonNode> parameterMap = new HashMap<>();
    for (JsonNode elem : paramRoot.path("parameters")) {
      parameterMap.put(elem.get("name").asText(), elem);
    }

    byte[] jsonData = Files.readAllBytes(componentDescriptionFile.toPath());
    JsonNode rootNode = objectMapper.readTree(jsonData);

    // get the name of this repository
    String repositoryName = rootNode.path("repository").asText();

    // get the array of components
    JsonNode components = rootNode.path("components");
    Iterator<JsonNode> componentsIt = components.elements();

    Component c;
    for (JsonNode component : components) {
      c = new Component(component.get("name").asText());
      // add provided interfaces
      for (JsonNode providedInterface : component.path("providedInterface")) {
        c.addProvidedInterface(providedInterface.asText());
      }

      // add required interfaces
      for (JsonNode requiredInterface : component.path("requiredInterface")) {
        c.addRequiredInterface(requiredInterface.asText());
      }

      Parameter p = null;
      Map<Parameter, ParameterRefinementConfiguration> paramConfig = new HashMap<>();

      for (JsonNode parameter : component.path("parameter")) {
        // name of the parameter
        String name = parameter.get("name").asText();
        // possible string params
        String[] stringParams = new String[] { "type" };
        String[] stringParamValues = new String[stringParams.length];
        // possible boolean params
        String[] boolParams = new String[] {};
        boolean[] boolParamValues = new boolean[boolParams.length];
        // possible double params
        String[] doubleParams = new String[] { "defaultValue", "min", "max", "refineSplits", "minInterval" };
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

        switch (stringParamValues[Arrays.stream(stringParams).collect(Collectors.toList()).indexOf("type")]) {
          case "int":
          case "double":
            p = new NumericParameter(name, stringParamValues[0].equals("int"), doubleParamValues[0], doubleParamValues[1], doubleParamValues[2]);
            paramConfig.put(p, new ParameterRefinementConfiguration((int) doubleParamValues[3], doubleParamValues[4]));
        }

        if (p != null) {
          c.addParameter(p);
        }

      }
      this.paramConfigs.put(c, paramConfig);
      this.components.add(c);
    }
  }

  public Map<Component, Map<Parameter, ParameterRefinementConfiguration>> getParamConfigs() {
    return this.paramConfigs;
  }

  public Collection<Component> getComponents() {
    return this.components;
  }

}
