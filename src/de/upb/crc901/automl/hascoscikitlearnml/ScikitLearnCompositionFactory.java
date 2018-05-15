package de.upb.crc901.automl.hascoscikitlearnml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import hasco.model.ComponentInstance;
import hasco.query.Factory;

public class ScikitLearnCompositionFactory implements Factory<ScikitLearnComposition> {

  @Override
  public ScikitLearnComposition getComponentInstantiation(final ComponentInstance groundComponent) {
    SLPipeline pipelineTree = this.buildSLPipelineTree(groundComponent);

    String pipelineToString = pipelineTree.toString();
    if (pipelineToString.contains("make_union")) {
      System.out.println("make_union");
      try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File("make_union.log"), true))) {
        bw.write(pipelineToString + "\n");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (pipelineToString.contains("make_forward")) {
      System.out.println("make_forward");
      try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File("make_forward.log"), true))) {
        bw.write(pipelineToString + "\n");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    try {
      return new ScikitLearnComposition();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private SLPipeline buildSLPipelineTree(final ComponentInstance ci) {
    StringBuilder codeBuilder = new StringBuilder();
    codeBuilder.append(ci.getComponent().getName());
    codeBuilder.append("(");
    boolean first = true;
    for (String paramName : ci.getParameterValues().keySet()) {
      if (first) {
        first = false;
      } else {
        codeBuilder.append(",");
      }
      codeBuilder.append(paramName + "=");
      String paramValue = ci.getParameterValues().get(paramName);
      try {
        Double.valueOf(paramValue);
        codeBuilder.append(ci.getParameterValues().get(paramName));
      } catch (Exception e) {
        codeBuilder.append("\"" + ci.getParameterValues().get(paramName) + "\"");
      }

    }
    codeBuilder.append(")");

    SLPipeline pipe = new SLPipeline(codeBuilder.toString());

    for (String key : ci.getSatisfactionOfRequiredInterfaces().keySet()) {
      pipe.addChild(this.buildSLPipelineTree(ci.getSatisfactionOfRequiredInterfaces().get(key)));
    }

    return pipe;
  }

}
