package hasco.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import hasco.model.Component;
import hasco.model.NumericParameter;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;

public class ComponentLoader {

  private Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramConfigs = new HashMap<>();
  private Collection<Component> components = new ArrayList<>();

  public ComponentLoader() {

  }

  public void loadComponents(final File jsonFile) throws IOException {
    byte[] jsonData = Files.readAllBytes(jsonFile.toPath());
    ObjectMapper objectMapper = new ObjectMapper();
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

      Parameter p;
      Map<Parameter, ParameterRefinementConfiguration> paramConfig = new HashMap<>();
      for (JsonNode parameter : component.path("parameter")) {
        switch (parameter.get("type").asText()) {
          case "int":
            p = new NumericParameter(parameter.get("name").asText(), true, parameter.get("default").asInt(), parameter.get("min").asInt(), parameter.get("max").asInt());
            paramConfig = new HashMap<>();
            paramConfig.put(p, new ParameterRefinementConfiguration(parameter.get("refineSplits").asInt(), parameter.get("minInterval").asInt()));
            c.addParameter(p);
            this.paramConfigs.put(c, paramConfig);
            break;
          case "double":
            p = new NumericParameter(parameter.get("name").asText(), false, parameter.get("default").asDouble(), parameter.get("min").asInt(), parameter.get("max").asInt());
            paramConfig.put(p, new ParameterRefinementConfiguration(parameter.get("refineSplits").asInt(), parameter.get("minInterval").asDouble()));
            c.addParameter(p);
            break;
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
