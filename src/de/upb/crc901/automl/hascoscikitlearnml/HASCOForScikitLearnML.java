package de.upb.crc901.automl.hascoscikitlearnml;

import de.upb.crc901.automl.hascoscikitlearnml.ScikitLearnBenchmark.BenchmarkMeasure;
import de.upb.crc901.automl.hascowekaml.HASCOForMEKA;

import jaicore.basic.IObjectEvaluator;
import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.core.INodeEvaluator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.core.HASCO.HASCOSolutionIterator;
import hasco.core.HASCOFD;
import hasco.core.Solution;
import hasco.serialization.ComponentLoader;
import weka.core.Instances;

public class HASCOForScikitLearnML implements IObservableGraphAlgorithm<TFDNode, String> {

  private static final Logger logger = LoggerFactory.getLogger(HASCOForMEKA.class);

  public static class HASCOForScikitLearnMLSolution {

    private Solution<ForwardDecompositionSolution, ScikitLearnComposition, Double> hascoSolution;

    public HASCOForScikitLearnMLSolution(final Solution<ForwardDecompositionSolution, ScikitLearnComposition, Double> hascoSolution) {
      super();
      this.hascoSolution = hascoSolution;
    }

    public ScikitLearnComposition getClassifier() {
      return this.hascoSolution.getSolution();
    }

    public double getScore() {
      return this.hascoSolution.getScore();
    }

    public int getTimeForScoreComputation() {
      return this.hascoSolution.getTimeToComputeScore();
    }
  }

  private boolean isCanceled = false;
  private int numberOfCPUs = 1;
  private Collection<Object> listeners = new ArrayList<>();
  private HASCOSolutionIterator hascoRun;
  private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator = n -> null;

  private Queue<HASCOForScikitLearnMLSolution> solutionsFoundByHASCO = new PriorityQueue<>(new Comparator<HASCOForScikitLearnMLSolution>() {
    @Override
    public int compare(final HASCOForScikitLearnMLSolution o1, final HASCOForScikitLearnMLSolution o2) {
      return (int) Math.round(10000 * (o1.getScore() - o2.getScore()));
    }
  });

  public void gatherSolutions(final Instances data, final int timeoutInMS) {
    if (this.isCanceled) {
      throw new IllegalStateException("HASCO has already been canceled. Cannot gather results anymore.");
    }

    long start = System.currentTimeMillis();
    long deadline = start + timeoutInMS;

    /* derive existing components */
    ComponentLoader cl = new ComponentLoader();
    try {
      cl.loadComponents(new File("testrsc/tpot/tpotComponents.json"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("Loaded components file!");

    /* create algorithm */
    // IObjectEvaluator<ScikitLearnComposition, Double> mccv = new
    // MonteCarloCrossValidationEvaluator(new F1AverageMultilabelEvaluator(new Random(0)), 1, data,
    // 0.7f);

    IObjectEvaluator<ScikitLearnComposition, Double> evaluator = new ScikitLearnBenchmark(BenchmarkMeasure.ACCURACY, data, 10, 0.7, timeoutInMS, 1);
    HASCOFD<ScikitLearnComposition> hasco = new HASCOFD<>(new ScikitLearnCompositionFactory(), this.preferredNodeEvaluator, cl.getParamConfigs(), "MLPipeline", evaluator);
    hasco.addComponents(cl.getComponents());
    hasco.setNumberOfCPUs(this.numberOfCPUs);

    /* add all listeners to HASCO */
    this.listeners.forEach(l -> hasco.registerListener(l));
    this.listeners.forEach(l -> hasco.registerListenerForSolutionEvaluations(l));

    /* run HASCO */
    this.hascoRun = hasco.iterator();
    boolean deadlineReached = false;
    while (!this.isCanceled && this.hascoRun.hasNext() && (timeoutInMS <= 0 || !(deadlineReached = System.currentTimeMillis() >= deadline))) {
      HASCOForScikitLearnMLSolution nextSolution = new HASCOForScikitLearnMLSolution(this.hascoRun.next());
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

  public Queue<HASCOForScikitLearnMLSolution> getFoundClassifiers() {
    return new LinkedList<>(this.solutionsFoundByHASCO);
  }

  public HASCOForScikitLearnMLSolution getCurrentlyBestSolution() {
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

  public int getNumberOfCPUs() {
    return this.numberOfCPUs;
  }

  public void setNumberOfCPUs(final int numberOfCPUs) {
    this.numberOfCPUs = numberOfCPUs;
  }

}
