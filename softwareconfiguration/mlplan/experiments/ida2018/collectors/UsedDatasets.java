package ida2018.collectors;

import jaicore.basic.chunks.Task;
import jaicore.basic.chunks.TaskChunk;
import jaicore.basic.kvstore.KVStoreUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import ida2018.IDA2018Util;
import weka.core.Instances;

public class UsedDatasets {

  public static void main(final String[] args) {

    Collection<String> datasets = IDA2018Util.getConsideredDatasets();

    TaskChunk<Task> datasetChunk = new TaskChunk<>("chunkID=datasetChunk");

    for (String dataset : datasets) {
      try {
        Instances data = new Instances(new FileReader(new File("../datasets/classification/multi-class/" + dataset)));
        data.setClassIndex(data.numAttributes() - 1);

        String key = "dataset";
        String keyValue = dataset.substring(0, dataset.lastIndexOf("."));

        String[] valueKeys = { "instances", "attributes", "classes" };
        int[] values = { data.size(), data.numAttributes() - 1, data.classAttribute().numValues() };

        for (int i = 0; i < valueKeys.length; i++) {
          Task t = new Task();
          datasetChunk.add(t);
          t.store(key, keyValue);
          t.store("name", valueKeys[i]);
          t.store("value", values[i]);
        }
      } catch (FileNotFoundException e) {
        System.err.println(dataset);
        e.printStackTrace();
      } catch (IOException e) {
        System.err.println(dataset);
        e.printStackTrace();
      }

    }

    String latexTable = KVStoreUtil.kvStoreCollectionToLaTeXTable(datasetChunk.toKVStoreCollection(), "dataset", "name", "value");
    System.out.println(latexTable);

  }

}
