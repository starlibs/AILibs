package de.upb.crc901.automl.hascocombinedml;

import de.upb.crc901.automl.pipeline.service.MLServicePipeline;

import jaicore.basic.IObjectEvaluator;
import jaicore.basic.MySQLAdapter;
import jaicore.basic.chunks.Task;
import jaicore.ml.WekaUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.aeonbits.owner.ConfigCache;

import weka.core.Instances;

public class MLServiceBenchmark implements IObjectEvaluator<MLServicePipeline, Double> {

  private static final HASCOForCombinedMLConfig CONFIG = ConfigCache.getOrCreate(HASCOForCombinedMLConfig.class);
  private static final Timer TIMEOUT_TIMER = new Timer();

  private Task runTask;
  private Instances data;
  private int maximumSeeds;
  private Double splitSize;
  private int repetitions;
  private MySQLAdapter mysql;
  private int timeoutInMS = -1;

  private List<Instances> trainTestSplit = null;

  public MLServiceBenchmark() {
  }

  public MLServiceBenchmark(final Instances data, final int repetitions, final double splitSize, final int timeoutInMS, final int maximumSeeds, final MySQLAdapter mysql,
      final String mysqlLogTable, final Task runTask) {
    this.data = data;
    this.splitSize = splitSize;
    this.maximumSeeds = maximumSeeds;
    this.repetitions = repetitions;
    this.mysql = mysql;
    this.runTask = runTask;
    this.timeoutInMS = timeoutInMS;
  }

  public MLServiceBenchmark(final List<Instances> trainTestSplit, final MySQLAdapter mysql, final String mysqlLogTable, final Task runTask) {
    this.trainTestSplit = trainTestSplit;
    this.mysql = mysql;
    this.runTask = runTask;
  }

  public Double evaluateFixedSplit(final MLServicePipeline object) throws Exception {
    return this.evaluate(object, this.trainTestSplit.get(0), this.trainTestSplit.get(1));
  }

  private Double evaluate(final MLServicePipeline pipeline, final Instances train, final Instances test) throws Exception {
    pipeline.buildClassifier(train);
    double[] prediction = pipeline.classifyInstances(test);

    double errorCounter = 0d;
    for (int i = 0; i < test.size(); i++) {
      if (prediction[i] != test.get(i).classValue()) {
        errorCounter++;
      }
    }
    return errorCounter / test.size();
  }

  @Override
  public Double evaluate(final MLServicePipeline object) throws Exception {
    if (Thread.interrupted()) {
      throw new InterruptedException("Got interrupted");
    }

    if (this.splitSize == null) {
      throw new IllegalArgumentException("Inappropriate use of MLServiceBenchmark. No split size provided.");
    }

    if (this.trainTestSplit != null) {
      return this.evaluateFixedSplit(object);
    }

    EvaluationTimeout timeout = new EvaluationTimeout(Thread.currentThread());
    if (this.timeoutInMS > 0) {
      TIMEOUT_TIMER.schedule(timeout, this.timeoutInMS);
    }
    double returnValue = 10000d;

    long startTime = System.currentTimeMillis();
    try {
      List<Double> errorRates = new LinkedList<>();
      for (int i = 0; i < ((this.maximumSeeds > 0) ? Math.min(this.maximumSeeds, this.repetitions) : this.repetitions); i++) {
        if (Thread.currentThread().isInterrupted()) {
          throw new InterruptedException("Thread got interrupted, so stop evaluation of composition object");
        }
        long splitSeed = (this.maximumSeeds <= 0) ? new Random().nextLong() : new Random().nextInt(this.maximumSeeds);

        List<Instances> mccvSplit = WekaUtil.getStratifiedSplit(this.data, new Random(splitSeed), this.splitSize);
        System.out.println("Evaluate " + object.getConstructionPlan());
        double errorRate = this.evaluate(object, mccvSplit.get(0), mccvSplit.get(1));
        System.out.println("Validated performance: " + errorRate);

        if (errorRate >= 0 && errorRate <= 1) {
          errorRates.add(errorRate);
        } else {
          break;
        }
      }
      timeout.cancel();
      if (errorRates.isEmpty()) {
        returnValue = 20000d;
      } else {
        if (errorRates.size() >= 5) {
          Collections.sort(errorRates);
          int numEvalsToRemove = (int) (Math.floor(errorRates.size() * 0.2));
          for (int i = 0; i < numEvalsToRemove; i++) {
            errorRates.remove(0);
            errorRates.remove(errorRates.size() - 1);
          }
        }
        returnValue = errorRates.stream().mapToDouble(x -> x).average().getAsDouble();
        // System.out.println("ErrorRate: " + returnValue + " " + object.getPipelineCode());
      }

    } catch (InterruptedException e) {
      returnValue = 10000d;
    }

    if (this.mysql != null) {
      Map<String, String> valueMap = new HashMap<>();
      valueMap.put("run_id", this.runTask.getValueAsString("run_id"));
      valueMap.put("pipeline", object.getConstructionPlan().toString());
      valueMap.put("errorRate", returnValue + "");
      valueMap.put("timeToSolution", (System.currentTimeMillis() - CONFIG.getRunStartTimestamp()) + "");
      valueMap.put("evaluationTime", (System.currentTimeMillis() - startTime) + "");

      this.mysql.insert("evaluation", valueMap);
    }
    return returnValue;
  }

  class EvaluationTimeout extends TimerTask {
    Thread threadToInterrupt;

    EvaluationTimeout(final Thread threadToInterrupt) {
      this.threadToInterrupt = threadToInterrupt;
    }

    @Override
    public void run() {
      this.threadToInterrupt.interrupt();
    }

  }
}
