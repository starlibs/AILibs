package jaicore.ml;

import jaicore.basic.SQLAdapter;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import jaicore.ml.core.evaluation.measure.singlelabel.ZeroOneLoss;
import jaicore.ml.evaluation.evaluators.weka.IClassifierEvaluator;
import jaicore.ml.evaluation.evaluators.weka.SingleRandomSplitClassifierEvaluator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.aeonbits.owner.ConfigCache;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class MLExperimentTester implements IExperimentSetEvaluator {

  private final ISpecificMLExperimentConfig config = ConfigCache.getOrCreate(ISpecificMLExperimentConfig.class);

  @Override
  public IExperimentSetConfig getConfig() {
    return this.config;
  }

  @Override
  public void evaluate(final ExperimentDBEntry experimentEntry, final SQLAdapter adapter, final IExperimentIntermediateResultProcessor processor) throws Exception {
    if (this.config.getDatasetFolder() == null || (!this.config.getDatasetFolder().exists())) {
      throw new IllegalArgumentException("config specifies invalid dataset folder " + this.config.getDatasetFolder());
    }
    Map<String, String> description = experimentEntry.getExperiment().getValuesOfKeyFields();
    Classifier c = AbstractClassifier.forName(description.get("classifier"), null);
    Instances data = new Instances(new BufferedReader(new FileReader(new File(this.config.getDatasetFolder() + File.separator + description.get("dataset") + ".arff"))));
    data.setClassIndex(data.numAttributes() - 1);
    int seed = Integer.valueOf(description.get("seed"));

    System.out.println(c.getClass().getName());
    Map<String, Object> results = new HashMap<>();
    SingleRandomSplitClassifierEvaluator eval = new SingleRandomSplitClassifierEvaluator(data);
    eval.setSeed(seed);
    double loss = eval.evaluate(c);

    results.put("loss", loss);
    processor.processResults(results);

  }

  public static void main(final String[] args) {
    ExperimentRunner runner = new ExperimentRunner(new MLExperimentTester());
    runner.randomlyConductExperiments(true);
  }

}
