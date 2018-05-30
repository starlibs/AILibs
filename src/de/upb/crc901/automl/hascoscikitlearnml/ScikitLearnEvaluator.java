package de.upb.crc901.automl.hascoscikitlearnml;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;

import org.aeonbits.owner.ConfigCache;

public class ScikitLearnEvaluator {
  private static final HASCOForScikitLearnMLConfig CONFIG = ConfigCache.getOrCreate(HASCOForScikitLearnMLConfig.class);

  public static Double evaluate(final File trainFile, final File testFile, final ScikitLearnComposition object, final boolean errorOutput)
      throws InterruptedException, IOException {
    if (!trainFile.exists() || !testFile.exists()) {
      throw new IllegalArgumentException("Train or test file does not exist. This must not happen.");
    }
    String cmd = "python " + object.getExecutable() + " " + trainFile.getName() + " " + testFile.getName();
    ProcessBuilder pb = new ProcessBuilder().command(cmd.split(" ")).directory(CONFIG.getTmpFolder());
    if (errorOutput) {
      pb.redirectError(Redirect.INHERIT);
    }
    Process p = pb.start();
    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
    String line;
    Double error = -1.0;
    while ((line = br.readLine()) != null) {
      try {
        error = Double.parseDouble(line);
      } catch (Exception e) {
        if (e instanceof NumberFormatException) {
          e.printStackTrace();
        } else {
          e.printStackTrace();
        }
      }
    }

    p.waitFor();
    return error;
  }

}
