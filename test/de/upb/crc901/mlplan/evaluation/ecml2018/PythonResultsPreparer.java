package de.upb.crc901.mlplan.evaluation.ecml2018;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class PythonResultsPreparer {

  private static final String SQL_TABLE = "coldstart_results";
  private static final String BASE_FOLDER_NAME = "results" + File.separator + "ecml" + File.separator + "asl_";
  private static final int TIMEOUT = 86400;
  private static final File RESULTS_FOLDER = new File(BASE_FOLDER_NAME + TIMEOUT);

  public static void main(final String[] args) throws IOException {
    Map<Integer, Double> errorRates = new HashMap<>();
    for (File file : RESULTS_FOLDER.listFiles()) {
      if (!file.isFile()) {
        continue;
      }

      String fileContent = FileUtils.readFileToString(file);
      Double accuracy = Double.valueOf(fileContent);
      int k = Integer.valueOf(file.getName().split("_")[0]);
      errorRates.put(k, (1 - accuracy));
    }

    StringBuffer sb = new StringBuffer();
    sb.append("DELETE FROM " + SQL_TABLE + " WHERE timeout=" + TIMEOUT + ";\n");

    sb.append("INSERT INTO " + SQL_TABLE + " (timeout,dataset,seed,errorRate) VALUES ");
    List<Integer> keys = new LinkedList<>(errorRates.keySet());
    Collections.sort(keys);

    boolean first = true;
    for (Integer k : keys) {
      if (first) {
        first = false;
      } else {
        sb.append(",");
      }
      sb.append("\n(" + TIMEOUT + ",'" + getDataset(k) + "', " + (k % 20) + "," + errorRates.get(k) + ")");
    }
    sb.append(";");

    System.out.println(sb.toString());
  }

  private static String getDataset(final int k) {
    String[] datasets = { "abalone", "amazon", "cars", "cifar10", "cifar10small", "convex", "credit-g", "dexter", "dorothea", "gisette", "kddcup09", "krvskp", "madelon", "mnist",
        "mnistrotationback", "secom", "semeion", "shuttle", "waveform", "wine", "yeast" };

    int datasetsIndex = k / 20;
    return datasets[datasetsIndex];
  }

}
