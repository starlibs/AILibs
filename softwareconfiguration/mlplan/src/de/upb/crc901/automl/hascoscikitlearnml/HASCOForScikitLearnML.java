package de.upb.crc901.automl.hascoscikitlearnml;

import de.upb.crc901.automl.hascowekaml.HASCOForMEKA;

import jaicore.basic.SQLAdapter;
import jaicore.graph.IObservableGraphAlgorithm;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.core.INodeEvaluator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.core.HASCO.HASCOSolutionIterator;
import hasco.core.HASCOFD;
import hasco.core.Solution;
import hasco.serialization.ComponentLoader;

public class HASCOForScikitLearnML implements IObservableGraphAlgorithm<TFDNode, String> {

  private static final Logger logger = LoggerFactory.getLogger(HASCOForMEKA.class);
  private static final HASCOForScikitLearnMLConfig CONFIG = ConfigCache.getOrCreate(HASCOForScikitLearnMLConfig.class);

  private Lock selectedSolutionsLock = new ReentrantLock();
  private AtomicInteger selectionTasksCounter = new AtomicInteger(0);
  private Double bestValidationScore = null;
  private static double EPSILON = 0.03;
  private static final int NUMBER_OF_CONSIDERED_SOLUTIONS = 100;

  public static class HASCOForScikitLearnMLSolution {

    private Solution<ForwardDecompositionSolution, ScikitLearnComposition, Double> hascoSolution;
    private Double selectionScore = null;
    private Double testScore = null;

    public HASCOForScikitLearnMLSolution(final Solution<ForwardDecompositionSolution, ScikitLearnComposition, Double> hascoSolution) {
      super();
      this.hascoSolution = hascoSolution;
    }

    public ScikitLearnComposition getClassifier() {
      return this.hascoSolution.getSolution();
    }

    public int getTimeForScoreComputation() {
      return this.hascoSolution.getTimeToComputeScore();
    }

    public Double getValidationScore() {
      return this.hascoSolution.getScore();
    }

    public void setSelectionScore(final double selectionScore) {
      this.selectionScore = selectionScore;
    }

    public Double getSelectionScore() {
      return this.selectionScore;
    }

    public void setTestScore(final double testScore) {
      this.testScore = testScore;
    }

    public Double getTestScore() {
      return this.testScore;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();

      sb.append(this.getClassifier().getPipelineCode());
      sb.append(" ");
      sb.append("Val: " + this.getValidationScore());
      sb.append(" ");
      sb.append("Sel: " + this.getSelectionScore());
      sb.append(" ");
      sb.append("Test: " + this.getTestScore());

      return sb.toString();
    }
  }

  private int numberOfCPUs = CONFIG.getCPUs();
  private int timeout = CONFIG.getTimeout();

  private boolean isCanceled = false;
  private Collection<Object> listeners = new ArrayList<>();
  private HASCOSolutionIterator hascoRun;

  private static final List<String> ORDERING_OF_CLASSIFIERS = Arrays.asList(new String[] { "sklearn.ensemble.GradientBoostingClassifier", "sklearn.ensemble.RandomForestClassifier",
      "sklearn.ensemble.ExtraTreesClassifier", "sklearn.neighbors.KNeighborsClassifier", "sklearn.svm.LinearSVC", "sklearn.linear_model.LogisticRegression",
      "sklearn.naive_bayes.GaussianNB", "sklearn.naive_bayes.BernoulliNB", "sklearn.tree.DecisionTreeClassifier", "sklearn.naive_bayes.MultinomialNB", "xgboost.XGBClassifier" });

  private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator = n -> {
    boolean containsClassifier = n.externalPath().stream()
        .anyMatch(x -> x.getAppliedMethodInstance() != null && (x.getAppliedMethodInstance().getMethod().getName().startsWith("resolveAbstractClassifierWith")
            && !x.getAppliedMethodInstance().getMethod().getName().endsWith("make_pipeline")));

    long counter = n.externalPath().stream()
        .filter(x -> x.getAppliedMethodInstance() != null && (x.getAppliedMethodInstance().getMethod().getName().startsWith("resolveAbstractPreprocessorWith")
            || x.getAppliedMethodInstance().getMethod().getName().startsWith("resolveBasicPreprocessorWith")))
        .count();

    if (containsClassifier && n.externalPath().get(n.externalPath().size() - 1).getAppliedMethodInstance() != null
        && n.externalPath().get(n.externalPath().size() - 1).getAppliedMethodInstance().getMethod().getName().startsWith("resolveAbstractClassifierWith")) {
      String methodName = n.externalPath().get(n.externalPath().size() - 1).getAppliedMethodInstance().getMethod().getName().substring(29);
      double indexOfClassifier = ORDERING_OF_CLASSIFIERS.indexOf(methodName) + 1;
      if (indexOfClassifier >= 0) {
        double fScore = indexOfClassifier / 100000;
        return fScore;
      }
    }

    if (counter < 1 && !containsClassifier) {
      return 0d;
    }

    return null;
  };
  BlockingQueue<Runnable> taskQueue = new PriorityBlockingQueue<>((int) (NUMBER_OF_CONSIDERED_SOLUTIONS * 1.5), new Comparator<Runnable>() {
    @Override
    public int compare(final Runnable o1, final Runnable o2) {
      if (o1 instanceof SelectionPhaseEval && o2 instanceof SelectionPhaseEval) {
        SelectionPhaseEval spe1 = (SelectionPhaseEval) o1;
        SelectionPhaseEval spe2 = (SelectionPhaseEval) o2;
        return spe1.solution.getValidationScore().compareTo(spe2.solution.getValidationScore());
      }
      return 0;
    }
  });
  private ExecutorService threadPool = new ThreadPoolExecutor(2, 2, 120, TimeUnit.SECONDS, this.taskQueue);

  private Queue<HASCOForScikitLearnMLSolution> solutionsFoundByHASCO = new PriorityQueue<>(new Comparator<HASCOForScikitLearnMLSolution>() {
    @Override
    public int compare(final HASCOForScikitLearnMLSolution o1, final HASCOForScikitLearnMLSolution o2) {
      return (int) Math.round(10000 * (o1.getValidationScore() - o2.getValidationScore()));
    }
  });
  private Queue<HASCOForScikitLearnMLSolution> solutionsSelectedByHASCO = new PriorityQueue<>(new Comparator<HASCOForScikitLearnMLSolution>() {
    @Override
    public int compare(final HASCOForScikitLearnMLSolution o1, final HASCOForScikitLearnMLSolution o2) {
      return (int) Math.round(10000 * (o1.getSelectionScore() - o2.getSelectionScore()));
    }
  });

  public void gatherSolutions(final ScikitLearnBenchmark searchBenchmark, final ScikitLearnBenchmark selectionBenchmark, final ScikitLearnBenchmark testBenchmark,
      final int timeoutInMS, final SQLAdapter mysql) {
    if (this.isCanceled) {
      throw new IllegalStateException("HASCO has already been canceled. Cannot gather results anymore.");
    }

    long start = System.currentTimeMillis();
    long deadline = start + timeoutInMS;

    /* derive existing components */
    ComponentLoader cl = new ComponentLoader();
    try {
      cl.loadComponents(CONFIG.getComponentFile());
    } catch (IOException e) {
      e.printStackTrace();
    }

    /* create algorithm */
    HASCOFD<ScikitLearnComposition,Double> hasco = new HASCOFD<>(cl.getComponents(), cl.getParamConfigs(), new ScikitLearnCompositionFactory(), CONFIG.getRequestedInterface(),
        searchBenchmark);
    hasco.setPreferredNodeEvaluator(preferredNodeEvaluator);
    hasco.setNumberOfCPUs(this.numberOfCPUs);
    hasco.setTimeout(CONFIG.getTimeout());

    /* add all listeners to HASCO */
    this.listeners.forEach(l -> hasco.registerListener(l));
    this.listeners.forEach(l -> hasco.registerListenerForSolutionEvaluations(l));

    /* run HASCO */
    this.hascoRun = hasco.iterator();
    boolean deadlineReached = false;
    while (!this.isCanceled && this.hascoRun.hasNext() && (timeoutInMS <= 0 || !(deadlineReached = System.currentTimeMillis() >= deadline))) {
      HASCOForScikitLearnMLSolution nextSolution = new HASCOForScikitLearnMLSolution(this.hascoRun.next());
      /* Skip returned solutions that obtained a timeout or were not able to be computed */
      if (nextSolution.getValidationScore() >= 10000) {
        continue;
      }
      System.out.println("Solution found " + nextSolution.getClassifier().getPipelineCode() + " " + nextSolution.getValidationScore());
      this.solutionsFoundByHASCO.add(nextSolution);
      List<HASCOForScikitLearnMLSolution> solutionList = new LinkedList<>(this.solutionsFoundByHASCO);

      if (this.bestValidationScore == null || nextSolution.getValidationScore() < this.bestValidationScore) {
        this.bestValidationScore = nextSolution.getValidationScore();
      }

      double coinFlipRatio = 1;
      if (this.selectionTasksCounter.get() > 10) {
        coinFlipRatio = (double) 10 / this.selectionTasksCounter.get();
      }

      double randomRatio = new Random(CONFIG.getSeed()).nextDouble();

      boolean coinFlip = randomRatio <= coinFlipRatio;
      if (solutionList.indexOf(nextSolution) <= (this.NUMBER_OF_CONSIDERED_SOLUTIONS / 2) || (nextSolution.getValidationScore() < this.bestValidationScore + EPSILON && coinFlip)) {
        int tasks = this.selectionTasksCounter.incrementAndGet();
        this.threadPool.submit(new SelectionPhaseEval(nextSolution, selectionBenchmark, testBenchmark, mysql));
        System.out.println("Selection tasks in Queue: " + this.taskQueue.size() + " (Submitted new task)");
      }

    }
    if (deadlineReached)

    {
      logger.info("Deadline has been reached");
    } else if (this.isCanceled) {
      logger.info("Interrupting HASCO due to cancel.");
    }
  }

  class SelectionPhaseEval implements Runnable {
    private HASCOForScikitLearnMLSolution solution;
    private ScikitLearnBenchmark benchmark;
    private ScikitLearnBenchmark test;
    private SQLAdapter mysql;

    public SelectionPhaseEval(final HASCOForScikitLearnMLSolution solution, final ScikitLearnBenchmark benchmark, final ScikitLearnBenchmark test, final SQLAdapter mysql) {
      this.solution = solution;
      this.benchmark = benchmark;
      this.test = test;
      this.mysql = mysql;
    }

    @Override
    public void run() {
      try {
        Double selectionError = this.benchmark.evaluate(this.solution.getClassifier());
        System.out.println("Performed selection phase for returned solution with selection error:" + selectionError);
        this.solution.setSelectionScore(selectionError);

        boolean newBest = false;
        HASCOForScikitLearnML.this.selectedSolutionsLock.lock();
        try {
          HASCOForScikitLearnML.this.solutionsSelectedByHASCO.add(this.solution);
          if (HASCOForScikitLearnML.this.solutionsSelectedByHASCO.peek() == this.solution) {
            newBest = true;
          }
        } finally {
          HASCOForScikitLearnML.this.selectedSolutionsLock.unlock();
        }
        if (newBest) {
          try {
            Double testPerformance = this.test.evaluateFixedSplit(this.solution.getClassifier());
            this.solution.setTestScore(testPerformance);

            System.out.println(this.solution);
            if (this.mysql != null) {
              Map<String, String> values = new HashMap<>();
              values.put("run_id", CONFIG.getRunID() + "");
              values.put("pipeline", this.solution.getClassifier().getPipelineCode());
              values.put("import", this.solution.getClassifier().getImportCode());
              values.put("pipelineComplexity", this.solution.getClassifier().getComplexity() + "");
              values.put("testErrorRate", this.solution.getTestScore() + "");
              values.put("validationErrorRate", this.solution.getValidationScore() + "");
              values.put("selectionErrorRate", this.solution.getSelectionScore() + "");
              values.put("timeToSolution", (System.currentTimeMillis() - CONFIG.getRunStartTimestamp()) + "");
              this.mysql.insert("experimentresult", values);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      } catch (Exception e) {
        this.solution.setSelectionScore(10000d);
        e.printStackTrace();
      }
      System.out.println("Selection tasks in Queue: " + HASCOForScikitLearnML.this.taskQueue.size() + " (Finished working on tasks)");
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

  public Queue<HASCOForScikitLearnMLSolution> getSelectedClassifiers() {
    return new LinkedList<>(this.solutionsSelectedByHASCO);
  }

  public HASCOForScikitLearnMLSolution getCurrentlyBestSolution() {
    if (!this.solutionsSelectedByHASCO.isEmpty()) {
      return this.solutionsSelectedByHASCO.peek();
    } else {
      return this.solutionsFoundByHASCO.peek();
    }
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

  public int getTimeout() {
    return this.timeout;
  }

  public void setTimeout(final int timeout) {
    this.timeout = timeout;
  }

}
