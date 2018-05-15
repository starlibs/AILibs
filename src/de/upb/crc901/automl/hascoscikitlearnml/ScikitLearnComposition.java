package de.upb.crc901.automl.hascoscikitlearnml;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ScikitLearnComposition {
  // universal ID counter for scikit learn compositions
  private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

  private final int compositionID;
  private final File executable;

  public ScikitLearnComposition() throws IOException {
    this.compositionID = ID_COUNTER.getAndIncrement();
    this.executable = new File("tmp/candidate_" + this.compositionID + ".py");

    Files.copy(new File("testrsc/hascoSL/template.py"), this.executable);
  }

  public String getExecutable() {
    return this.executable.getName();
  }

}
