package de.upb.crc901.automl.hascowekaml;

import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.MulticlassEvaluator;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.core.INodeEvaluator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.core.HASCO.HASCOSolutionIterator;
import hasco.core.HASCOFD;
import hasco.core.Solution;
import hasco.model.Component;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class HASCOForWekaML implements IObservableGraphAlgorithm<TFDNode, String> {

  private static final Logger logger = LoggerFactory.getLogger(HASCOForWekaML.class);

  public static class HASCOForWekaMLSolution {

    private Solution<ForwardDecompositionSolution, Classifier, Double> hascoSolution;

    public HASCOForWekaMLSolution(final Solution<ForwardDecompositionSolution, Classifier, Double> hascoSolution) {
      super();
      this.hascoSolution = hascoSolution;
    }

    public Classifier getClassifier() {
      return this.hascoSolution.getSolution();
    }
  }

  private boolean isCanceled = false;
  private Collection<Object> listeners = new ArrayList<>();
  private HASCOSolutionIterator hascoRun;
  private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator = n -> {

    for (TFDNode p : n.externalPath()) {
      if (p.getAppliedMethodInstance() != null) {
        System.out.println(p.getAppliedMethodInstance().getMethod().getName());
      }
    }
    System.out.println();
    // n.externalPath().stream().anyMatch(x -> x.getAppliedMethodInstance() != null &&
    // x.getAppliedMethodInstance().getMethod().getName().equals(""));
    return null;
  };

  private Queue<HASCOForWekaMLSolution> solutionsFoundByHASCO = new PriorityQueue<>(new Comparator<HASCOForWekaMLSolution>() {

    @Override
    public int compare(final HASCOForWekaMLSolution o1, final HASCOForWekaMLSolution o2) {
      return (int) Math.round(10000 * (HASCOForWekaML.this.getQualityHASCODeterminedForSolution(o2) - HASCOForWekaML.this.getQualityHASCODeterminedForSolution(o1)));
    }
  });

  public void gatherSolutions(final Instances data, final int timeoutInMS) {

    if (this.isCanceled) {
      throw new IllegalStateException("HASCO has already been canceled. Cannot gather results anymore.");
    }

    long start = System.currentTimeMillis();
    long deadline = start + timeoutInMS;

    /* create algorithm */
    Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramConfigs = new HashMap<>();
    HASCOFD<Classifier> hasco = new HASCOFD<>(groundComponent -> {
      Component component = groundComponent.getComponent();
      Map<String, String> paramValues = groundComponent.getParameterValues();
      String className = component.getName();
      try {
        List<String> params = new ArrayList<>();
        for (Parameter p : component.getParameters()) {
          if (paramValues.containsKey(p.getName())) {
            params.add("-" + p.getName());
            params.add(paramValues.get(p.getName()));
          }
        }
        String[] paramsAsArray = params.toArray(new String[] {});
        return AbstractClassifier.forName(className, paramsAsArray);
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }, this.preferredNodeEvaluator, paramConfigs, "classifier", new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(new Random(3)), 3, data, .7f));

    /* configuring existing components */
    Component c = null;
    Parameter p;
    Map<Parameter, ParameterRefinementConfiguration> paramConfig;
    c = new Component("weka.classifiers.trees.RandomTree");
    c.addProvidedInterface("classifier");
    c.addProvidedInterface("baseclassifier");
    p = new Parameter("M", new NumericParameterDomain(true, 1, 10), 1);
    paramConfig = new HashMap<>();
    paramConfig.put(p, new ParameterRefinementConfiguration(8, 1));
    c.addParameter(p);
    p = new Parameter("K", new NumericParameterDomain(true, 0, 10), 0);
    paramConfig.put(p, new ParameterRefinementConfiguration(2, 1));
    c.addParameter(p);
    paramConfigs.put(c, paramConfig);
    hasco.addComponent(c);

    /* add all listeners to HASCO */
    this.listeners.forEach(l -> hasco.registerListener(l));

    /* run HASCO */
    this.hascoRun = hasco.iterator();
    boolean deadlineReached = false;
    while (!this.isCanceled && this.hascoRun.hasNext() && (timeoutInMS <= 0 || !(deadlineReached = System.currentTimeMillis() >= deadline))) {
      HASCOForWekaMLSolution nextSolution = new HASCOForWekaMLSolution(this.hascoRun.next());
      this.solutionsFoundByHASCO.add(nextSolution);
    }
    if (deadlineReached) {
      logger.info("Deadline has been reached");
    } else if (this.isCanceled) {
      logger.info("Interrupting HASCO due to cancel.");
    }
  }

  public void cancel() {
    this.isCanceled = true;
    if (this.hascoRun != null) {
      this.hascoRun.cancel();
    }
  }

  public Queue<HASCOForWekaMLSolution> getFoundClassifiers() {
    return new LinkedList<>(this.solutionsFoundByHASCO);
  }

  public Map<String, Object> getAnnotationsOfSolution(final HASCOForWekaMLSolution solution) {
    return new HashMap<>();
  }

  public int getTimeHASCONeededToEvaluateSolution(final HASCOForWekaMLSolution solution) {
    return (int) this.getAnnotationsOfSolution(solution).get("fTime");
  }

  public double getQualityHASCODeterminedForSolution(final HASCOForWekaMLSolution solution) {
    return (double) this.getAnnotationsOfSolution(solution).get("f");
  }

  public HASCOForWekaMLSolution getCurrentlyBestSolution() {
    return this.solutionsFoundByHASCO.peek();
  }

  @Override
  public void registerListener(final Object listener) {
    this.listeners.add(listener);
  }

  public INodeEvaluator<TFDNode, Double> getPreferredNodeEvaluator() {
    return this.preferredNodeEvaluator;
  }

  public void setPreferredNodeEvaluator(final INodeEvaluator<TFDNode, Double> preferredNodeEvaluator) {
    this.preferredNodeEvaluator = preferredNodeEvaluator;
  }
}
