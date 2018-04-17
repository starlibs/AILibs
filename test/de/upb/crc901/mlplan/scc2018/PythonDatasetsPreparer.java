package de.upb.crc901.mlplan.scc2018;

import jaicore.ml.WekaUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

public class PythonDatasetsPreparer {

  public static void main(final String[] args) throws Exception {

    if (args.length != 3) {
      System.err.println(
          "Benchmark must receive 2 inputs: 1) the folder with the datasets, 2) Proportion of train/test 3) the number of samples per dataset; from this, we compute the dataset, the random sett and the respective splits");
      System.exit(0);
    }

    File folder = new File(args[0]);
    double proportion = Double.valueOf(args[1]);
    int numberOfSamples = Integer.valueOf(args[2]);

    System.out.println("Available datasets: ");
    Arrays.stream(folder.listFiles()).filter(x -> x.getName().endsWith(".arff")).map(x -> x.getName()).forEach(System.out::println);

    /* determine datasets and algorithms possibly used in the experiments */
    AtomicInteger k = new AtomicInteger(0);

    try (BufferedWriter el = new BufferedWriter(new FileWriter("experimentList.txt", true))) {
      el.write("dataset;seed;k\n");

      for (File datasetFile : folder.listFiles()) {
        if (!datasetFile.isFile() || !datasetFile.getName().endsWith(".arff")) {
          continue;
        }

        Instances data = new Instances(new BufferedReader(new FileReader(datasetFile)));
        data.setClassIndex(data.numAttributes() - 1);

        if (data.get(0) instanceof SparseInstance) {
          Instances convertedData = new Instances(data, data.numInstances());
          for (Instance i : data) {
            convertedData.add(new DenseInstance(i));
          }
          convertedData.setClassIndex(convertedData.numAttributes() - 1);
          data = convertedData;
        }

        final Instances dataToUse = data;

        IntStream.range(0, numberOfSamples).forEach(seed -> {
          /* create random object */
          Random r = new Random(seed);
          List<Instances> overallSplit = WekaUtil.getStratifiedSplit(dataToUse, r, proportion);
          Instances internalData = overallSplit.get(0);
          Instances testData = overallSplit.get(1);
          System.out.println(
              "Created split" + (seed + 1) + "/" + numberOfSamples + "for " + datasetFile.getName() + ": " + "Data was split into " + internalData.size() + "/" + testData.size());

          System.out.println("Write data to files");
          int currentK = k.getAndIncrement();

          try (BufferedWriter bw = new BufferedWriter(new FileWriter(currentK + "_train.arff"))) {
            bw.write(internalData.toString());
          } catch (IOException e) {
            e.printStackTrace();
          }

          try (BufferedWriter bw = new BufferedWriter(new FileWriter(currentK + "_test.arff"))) {
            bw.write(testData.toString());
          } catch (IOException e) {
            e.printStackTrace();
          }

          try {
            el.write(datasetFile.getName() + ";" + seed + ";" + currentK + "\n");
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
      }

    }

  }

}
