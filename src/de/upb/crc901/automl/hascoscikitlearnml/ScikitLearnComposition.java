package de.upb.crc901.automl.hascoscikitlearnml;

import com.google.common.io.Files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.aeonbits.owner.ConfigCache;

import hasco.model.ComponentInstance;

public class ScikitLearnComposition {
  private static final HASCOForScikitLearnMLConfig CONFIG = ConfigCache.getOrCreate(HASCOForScikitLearnMLConfig.class);

  // universal ID counter for scikit learn compositions
  private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

  private final int compositionID;
  private final File executable;
  private final Map<String, String> placeholderValueMap;
  private final ComponentInstance ci;
  private int testID = -1;

  public ScikitLearnComposition(final Map<String, String> pipelineSourceCodeMap, final ComponentInstance ci) throws IOException {
    this.compositionID = ID_COUNTER.getAndIncrement();
    this.executable = new File(CONFIG.getTmpFolder().getAbsolutePath() + File.separator + CONFIG.getCandidateScriptName() + "_" + this.compositionID + ".py");
    this.placeholderValueMap = pipelineSourceCodeMap;
    this.ci = ci;
    Files.copy(new File("testrsc/hascoSL/template.py"), this.executable);
    String templateCode = Files.toString(this.executable, Charset.defaultCharset());
    for (String placeholderName : this.placeholderValueMap.keySet()) {
      templateCode = templateCode.replaceAll("#" + placeholderName + "#", this.placeholderValueMap.get(placeholderName));
    }
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.executable))) {
      bw.write(templateCode);
      bw.flush();
    }
  }

  public String getExecutable() {
    return this.executable.getName();
  }

  public String getPipelineCode() {
    return this.placeholderValueMap.get("pipeline");
  }

  public String getImportCode() {
    return this.placeholderValueMap.get("import");
  }

  public double getComplexity() {
    int count = 0;
    Queue<ComponentInstance> ciList = new LinkedList<>();
    ciList.add(this.ci);

    ComponentInstance current;
    while ((current = ciList.poll()) != null) {
      count++;
      for (Entry<String, ComponentInstance> reqComp : current.getSatisfactionOfRequiredInterfaces().entrySet()) {
        if (!reqComp.getKey().equals("estimator") && !reqComp.getKey().equals("score_func")) {
          ciList.add(reqComp.getValue());
        }
      }
    }

    if (count > 1) {
      return 1.0 - (1 / Math.log(count));
    } else {
      return 1d;
    }
  }

  public void setTestID(final int id) {
    this.testID = id;
  }

  public int getTestID() {
    return this.testID;
  }

}
