package de.upb.crc901.automl.hascoscikitlearnml;

import jaicore.basic.IObjectEvaluator;
import jaicore.ml.WekaUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import weka.core.Instances;

public class ScikitLearnBenchmark implements IObjectEvaluator<ScikitLearnComposition, Double> {

  public enum BenchmarkMeasure {
    ACCURACY;
  }

  private final BenchmarkMeasure measure;
  private final Instances data;
  private final int timeoutInMS;
  private final int maximumSeeds;
  private final double splitSize;
  private final int repetitions;

  public ScikitLearnBenchmark(final BenchmarkMeasure measure, final Instances data, final int repetitions, final double splitSize, final int timeoutInMS, final int maximumSeeds) {
    this.measure = measure;
    this.data = data;
    this.splitSize = splitSize;
    this.timeoutInMS = timeoutInMS;
    this.maximumSeeds = maximumSeeds;
    this.repetitions = repetitions;
  }

  @Override
  public Double evaluate(final ScikitLearnComposition object) throws Exception {
    List<Double> errorRates = new LinkedList<>();
    for (int i = 0; i < ((this.maximumSeeds > 0) ? Math.min(this.maximumSeeds, this.repetitions) : this.repetitions); i++) {
      long splitSeed = (this.maximumSeeds <= 0) ? new Random().nextLong() : new Random().nextInt(this.maximumSeeds);
      File trainFile = new File("tmp" + File.separator + splitSeed + "_train.arff");
      File testFile = new File("tmp" + File.separator + splitSeed + "_test.arff");

      if (!trainFile.exists() || !testFile.exists()) {
        List<Instances> mccvSplit = WekaUtil.getStratifiedSplit(this.data, new Random(splitSeed), this.splitSize);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(trainFile))) {
          bw.write(mccvSplit.get(0).toString());
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(testFile))) {
          bw.write(mccvSplit.get(1).toString());
        }
      }

      String cmd = "python " + "tmp/" + object.getExecutable() + " " + trainFile.getName() + " " + testFile.getName();
      ProcessBuilder pb = new ProcessBuilder().redirectError(Redirect.INHERIT).command(cmd.split(" "));
      Process p = pb.start();
      BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line;

      Double error = 1.0;
      while ((line = br.readLine()) != null) {
        try {
          error = Double.parseDouble(line);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      errorRates.add(error);
      p.waitFor();
    }

    return errorRates.stream().mapToDouble(x -> x).average().getAsDouble();
  }

}
