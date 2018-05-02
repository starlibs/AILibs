package hasco.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import hasco.model.BooleanParameter;
import hasco.model.CategoricalParameter;
import hasco.model.Component;
import hasco.model.NumericParameter;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;

public class ComponentLoader {

  private Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramConfigs = new HashMap<>();
  private Collection<Component> components = new ArrayList<>();
  private final Set<String> parsedFiles = new HashSet<>();
  private final ObjectMapper objectMapper;
  private Map<String, JsonNode> parameterMap = new HashMap<>();

  public ComponentLoader() {
    this.objectMapper = new ObjectMapper();
  }

  private void parseFile(final File jsonFile) throws IOException {
    System.out.println("Parse file " + jsonFile.getAbsolutePath());
    StringBuilder stringDescriptionSB = new StringBuilder();
    String line;
    try (BufferedReader br = new BufferedReader(new FileReader(jsonFile))) {
      while ((line = br.readLine()) != null) {
        stringDescriptionSB.append(line + "\n");
      }
    }
    String jsonDescription = stringDescriptionSB.toString();
    jsonDescription = jsonDescription.replaceAll("/\\*(.*)\\*/", "");

    JsonNode rootNode = this.objectMapper.readTree(jsonDescription);

    for (JsonNode elem : rootNode.path("parameters")) {
      this.parameterMap.put(elem.get("name").asText(), elem);
    }
    JsonNode includes = rootNode.path("include");

    File baseFolder = new File(jsonFile.getCanonicalPath());
    if (jsonFile.isFile()) {
      baseFolder = new File(jsonFile.getCanonicalFile().getParentFile().getCanonicalPath());
    }

    for (JsonNode includePathNode : includes) {
      String path = includePathNode.asText();
      System.out.println("Include " + path);
      File subFile = new File(baseFolder.getAbsolutePath() + File.separator + path);
      if (!this.parsedFiles.contains(subFile.getCanonicalPath())) {
        if (subFile.isFile()) {
          this.parseFile(subFile.getCanonicalFile());
          this.parsedFiles.add(subFile.getCanonicalPath());
        } else {
          for (File subsubFile : subFile.listFiles()) {
            if (!this.parsedFiles.contains(subsubFile.getCanonicalPath()) && subsubFile.isFile() && subsubFile.getName().endsWith(".json")) {
              this.parseFile(subsubFile.getCanonicalFile());
              this.parsedFiles.add(subsubFile.getCanonicalPath());
            }
          }
          this.parsedFiles.add(subFile.getCanonicalPath());
        }
      }
    }
    // get the array of components
    JsonNode components = rootNode.path("components");
    if (components != null) {
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
          String[] stringParams = new String[] { "type", "values", "default" };
          String[] stringParamValues = new String[stringParams.length];
          // possible boolean params
          String[] boolParams = new String[] { "default" };
          boolean[] boolParamValues = new boolean[boolParams.length];
          // possible double params
          String[] doubleParams = new String[] { "default", "min", "max", "refineSplits", "minInterval" };
          double[] doubleParamValues = new double[doubleParams.length];

          if (this.parameterMap.containsKey(name)) {
            JsonNode commonParameter = this.parameterMap.get(name);
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
              break;
            case "bool":
              p = new BooleanParameter(name, boolParamValues[0]);
              break;
            case "cat":
              if (parameter.get("values").isTextual()) {
                p = new CategoricalParameter(name, Arrays.stream(stringParamValues[1].split(",")).collect(Collectors.toList()), stringParams[2]);
              } else {
                List<String> values = new LinkedList<>();
                for (JsonNode value : parameter.get("values")) {
                  values.add(value.asText());
                }
                p = new CategoricalParameter(name, values, stringParams[2]);
              }
              break;
          }

          if (p != null) {
            c.addParameter(p);
          }

        }
        this.paramConfigs.put(c, paramConfig);
        this.components.add(c);
      }
    }
  }

  public void loadComponents(final File componentDescriptionFile) throws IOException {
    this.paramConfigs.clear();
    this.components.clear();

    this.parseFile(componentDescriptionFile);

    System.out.println(this.components);
  }

  public Map<Component, Map<Parameter, ParameterRefinementConfiguration>> getParamConfigs() {
    return this.paramConfigs;
  }

  public Collection<Component> getComponents() {
    return this.components;
  }

  public static void main(final String[] args) throws IOException {
    ComponentLoader cl = new ComponentLoader();
    cl.loadComponents(new File("complexMLComponents.json"));

  }

}
