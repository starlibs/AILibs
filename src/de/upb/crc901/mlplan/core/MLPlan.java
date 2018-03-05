package de.upb.crc901.mlplan.core;

import de.upb.crc901.mlplan.classifiers.TwoPhaseHTNBasedPipelineSearcher;
import de.upb.crc901.mlplan.search.evaluators.BalancedRandomCompletionEvaluator;
import de.upb.crc901.mlplan.search.evaluators.MonteCarloCrossValidationEvaluator;
import de.upb.crc901.mlplan.search.evaluators.MulticlassEvaluator;

import java.io.File;
import java.util.Enumeration;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;

public class MLPlan extends AbstractClassifier implements Classifier, OptionHandler {

  private TwoPhaseHTNBasedPipelineSearcher<Double> searcher = new TwoPhaseHTNBasedPipelineSearcher<>();

  public MLPlan() {
    super();
  }

  @Override
  public void buildClassifier(final Instances data) throws Exception {
    Random random = this.searcher.getRandom();
    int timeout = this.searcher.getTimeout();
    this.searcher.setHtnSearchSpaceFile(new File("testrsc/automl-3.testset"));
    this.searcher.setNumberOfCPUs(4);
    MonteCarloCrossValidationEvaluator solutionEvaluator = new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(random), 3, .7f);
    this.searcher.setSolutionEvaluatorFactory4Search(() -> solutionEvaluator);
    this.searcher.setSolutionEvaluatorFactory4Selection(() -> new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(random), 10, .7f));
    this.searcher.setRce(new BalancedRandomCompletionEvaluator(random, 3, solutionEvaluator));
    this.searcher.setTimeoutPerNodeFComputation(1000 * (timeout == 60 ? 15 : 300));
    this.searcher.buildClassifier(data);
  }

  @Override
  public double classifyInstance(final Instance instance) throws Exception {
    return this.searcher.classifyInstance(instance);
  }

  @Override
  public double[] distributionForInstance(final Instance instance) throws Exception {
    return this.searcher.distributionForInstance(instance);
  }

  @Override
  public Capabilities getCapabilities() {
    Capabilities result = super.getCapabilities();
    result.disableAll();

    // attributes
    result.enable(Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capability.DATE_ATTRIBUTES);
    result.enable(Capability.STRING_ATTRIBUTES);
    result.enable(Capability.RELATIONAL_ATTRIBUTES);
    result.enable(Capability.MISSING_VALUES);

    // class
    result.enable(Capability.NOMINAL_CLASS);
    result.enable(Capability.NUMERIC_CLASS);
    result.enable(Capability.DATE_CLASS);
    result.enable(Capability.MISSING_CLASS_VALUES);

    // instances
    result.setMinimumNumberInstances(1);
    return result;
  }

  @Override
  public Enumeration<Option> listOptions() {
    return null;
  }

  @Override
  public void setOptions(final String[] options) throws Exception {
    for (int i = 0; i < options.length; i++) {
      switch (options[i].toLowerCase()) {
        case "-t": {
          this.setTimeout(Integer.parseInt(options[++i]));
          break;
        }
        case "-r": {
          this.setRandom(new Random(Integer.parseInt(options[++i])));
          break;
        }
        default: {
          throw new IllegalArgumentException("Unknown option " + options[i] + ".");
        }
      }
    }
  }

  @Override
  public String[] getOptions() {
    return null;
  }

  public void setTimeout(final int timeoutInMs) {
    this.searcher.setTimeout(timeoutInMs);
  }

  public void setRandom(final Random random) {
    this.searcher.setRandom(random);
  }

}
