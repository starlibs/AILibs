package hasco.tpotspace;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TPOTSpaceParser {

  private static final String PATH_TPOT_INPUT = "testrsc/tpot/tpot-classifiers.json";
  private static final String PATH_HASCO_OUT = "testrsc/tpot/hasco-tpot-classifiers.json";

  private static final String REPOSITORY = "TPOT-SEARCHSPACE";
  private static final int REFINEMENTS_PER_STEP = 2;

  private final ObjectMapper mapper = new ObjectMapper();
  private final ObjectNode rootNode;
  private final ArrayNode componentsArray;
  private final Set<String> availableComponents;

  public static void main(final String[] args) throws JsonProcessingException, IOException {
    new TPOTSpaceParser();
  }

  public TPOTSpaceParser() throws JsonProcessingException, IOException {
    JsonNode tpotClassifierDescription = this.mapper.readTree(new File(PATH_TPOT_INPUT));

    this.componentsArray = this.mapper.createArrayNode();

    this.rootNode = this.mapper.createObjectNode();
    this.rootNode.put("repository", REPOSITORY);
    this.rootNode.put("components", this.componentsArray);

    this.availableComponents = new HashSet<>();
    Iterator<String> componentsRegisterIt = tpotClassifierDescription.fieldNames();
    while (componentsRegisterIt.hasNext()) {
      this.availableComponents.add(componentsRegisterIt.next());
    }

    String fieldName;
    Iterator<String> fieldNamesIt = tpotClassifierDescription.fieldNames();
    while (fieldNamesIt.hasNext()) {
      fieldName = fieldNamesIt.next();
      JsonNode specification = tpotClassifierDescription.get(fieldName);
      ObjectNode component = this.createComponent(fieldName, specification);
      this.componentsArray.add(component);
    }
    // System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode));

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File("tpotComponents.json")))) {
      bw.write(this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.rootNode));
    }

  }

  private ObjectNode createComponent(final String name, final JsonNode componentDescription) {
    ObjectNode componentNode = this.mapper.createObjectNode();
    componentNode.put("name", name);

    ArrayNode providedInterface = this.mapper.createArrayNode();
    componentNode.put("providedInterface", providedInterface);
    providedInterface.add(name);

    if (name.contains("feature_selection")) {
      providedInterface.add("FeatureSelection");
    }
    if (name.contains("decomposition")) {
      providedInterface.add("DecompositionPreprocessing");
    }
    if (name.contains("preprocessing")) {
      providedInterface.add("Preprocessing");
    }
    if (name.contains("cluster")) {
      providedInterface.add("AgglomerationPreprocessing");
    }

    Set<String> preprocessors = new HashSet<>();
    preprocessors.add("feature_selection");
    preprocessors.add("decomposition");
    preprocessors.add("preprocessing");
    preprocessors.add("cluster");

    if (name.contains("ensemble")) {
      providedInterface.add("EnsembleClassifier");
    }

    if (preprocessors.stream().anyMatch(name::contains)) {
      providedInterface.add("AbstractPreprocessor");
    } else {
      providedInterface.add("AbstractClassifier");
    }

    ArrayNode requiredInterface = this.mapper.createArrayNode();
    componentNode.put("requiredInterface", requiredInterface);

    ArrayNode parametersNode = this.mapper.createArrayNode();
    componentNode.put("parameter", parametersNode);

    Iterator<String> parameterIt = componentDescription.fieldNames();
    while (parameterIt.hasNext()) {
      boolean addedParamValues = false;
      String parameterName = parameterIt.next();
      JsonNode parameterValue = componentDescription.get(parameterName);

      ObjectNode parameterNode = this.mapper.createObjectNode();
      parameterNode.put("name", parameterName);

      if (parameterValue.isTextual()) {
        String parameterValueText = parameterValue.asText();
        // check whether the parameter is specifed via arange
        if (parameterValueText.startsWith("np.arange")) {
          String[] paramSpec = parameterValueText.substring(10, parameterValueText.length() - 1).split(",");
          double min = Double.valueOf(paramSpec[0]);
          double max = Double.valueOf(paramSpec[1]);
          double step = Double.valueOf(paramSpec[2]);
          double defaultValue = min + ((max - min) / 2);

          parameterNode.put("type", "double");
          parameterNode.put("default", defaultValue);
          parameterNode.put("min", min);
          parameterNode.put("max", max);
          parameterNode.put("minInterval", step);
          parameterNode.put("refineSplits", REFINEMENTS_PER_STEP);
          addedParamValues = true;
        }
        // check whether parameter is specified via simple range
        else if (parameterValueText.startsWith("range")) {
          String[] paramSpec = parameterValueText.substring(6, parameterValueText.length() - 1).split(",");
          int min = Integer.valueOf(paramSpec[0].trim());
          int max = Integer.valueOf(paramSpec[1].trim());
          int defaultValue = min + (int) ((double) (max - min) / 2);

          parameterNode.put("type", "int");
          parameterNode.put("default", defaultValue);
          parameterNode.put("min", min);
          parameterNode.put("max", max);
          parameterNode.put("minInterval", 1);
          parameterNode.put("refineSplits", REFINEMENTS_PER_STEP);
          addedParamValues = true;
        }
      } else if (parameterValue.isArray()) {
        JsonNode firstArrayValue = parameterValue.elements().next();
        parameterNode.put("default", firstArrayValue);

        // count the number of elements
        int countElements = 0;
        for (JsonNode child : parameterValue) {
          countElements++;
        }

        if (firstArrayValue.isTextual() && (firstArrayValue.asText().equals("true") || firstArrayValue.asText().equals("false")) && countElements == 2) {
          parameterNode.put("type", "boolean");
          addedParamValues = true;
        } else if (firstArrayValue.isNumber() || firstArrayValue.isTextual()) {
          parameterNode.put("type", "cat");
          parameterNode.put("values", parameterValue);
          addedParamValues = true;
        }
      } else if (parameterValue.isObject()) {
        if (this.availableComponents.contains(parameterValue.fieldNames().next())) {
          requiredInterface.add(parameterValue.fieldNames().next());
        } else {
          String subComponentName = parameterValue.fieldNames().next();
          JsonNode subComponentDescription = parameterValue.elements().next();
          ObjectNode subComponent = this.createComponent(subComponentName, subComponentDescription);
          this.availableComponents.add(subComponentName);
          this.componentsArray.add(subComponent);
          requiredInterface.add(subComponentName);
        }
      }

      if (addedParamValues) {
        parametersNode.add(parameterNode);
      }
    }

    return componentNode;
  }

}
