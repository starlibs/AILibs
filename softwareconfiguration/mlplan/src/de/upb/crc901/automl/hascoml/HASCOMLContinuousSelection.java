package de.upb.crc901.automl.hascoml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.automl.PreferenceBasedNodeEvaluator;
import de.upb.crc901.automl.hascowekaml.HASCOClassificationML;
import hasco.core.HASCO;
import hasco.core.HASCOFD;
import hasco.query.Factory;
import hasco.serialization.ComponentLoader;
import jaicore.basic.FileUtil;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.graph.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.logging.LoggerUtil;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.Node;

public class HASCOMLContinuousSelection<C> implements IObservableGraphAlgorithm<TFDNode, String>, ILoggingCustomizable {

	// debugging tools
	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(HASCOClassificationML.class);

	class SelectionPhaseEval implements Runnable {
		private HASCOMLContinuousSelectionSolution<C> solution;

		public SelectionPhaseEval(final HASCOMLContinuousSelectionSolution<C> solution) {
			this.solution = solution;
		}

		@Override
		public void run() {
			try {
				// compute the performance of the solution with respect to the select benchmark
				Double selectionError = HASCOMLContinuousSelection.this.selectBenchmark
						.evaluate(this.solution.getSolution());
				this.solution.setSelectionScore(selectionError);
				HASCOMLContinuousSelection.this.logger.debug(
						"Performed selection phase for returned solution with selection error:" + selectionError);

				// add the solution the list of solutions evaluated w.r.t. the select benchmark
				// and test whether we have a new best performing candidate
				boolean newBest = false;
				HASCOMLContinuousSelection.this.selectedSolutionsLock.lock();
				try {
					HASCOMLContinuousSelection.this.solutionsSelectedByHASCO.add(this.solution);
					if (HASCOMLContinuousSelection.this.solutionsSelectedByHASCO.peek() == this.solution) {
						newBest = true;
					}
				} finally {
					HASCOMLContinuousSelection.this.selectedSolutionsLock.unlock();
				}

				// if we have a new best solution estimate its generalization performance
				// evaluating it on the test data
				if (newBest) {
					try {
						Double testPerformance = HASCOMLContinuousSelection.this.testBenchmark
								.evaluate(this.solution.getSolution());
						this.solution.setTestScore(testPerformance);
						HASCOMLContinuousSelection.this.logger.debug("Test Score of solution " + this.solution);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				this.solution.setSelectionScore(10000d);
				e.printStackTrace();
			}
			HASCOMLContinuousSelection.this.logger.debug("Selection tasks in Queue: "
					+ HASCOMLContinuousSelection.this.taskQueue.size() + " (Finished working on tasks)");
		}

	}

	// runtime configurations
	private final long seed;
	private final String requestedInterface;

	private int timeout;
	private int numberOfSearchCPUs = 7;
	private int numberOfSelectionCPUs = 1;
	private int numberOfConsideredSolutions = 100;
	private double EPSILON = 0.03;

	// state variables
	private boolean isCanceled = false;
	private Lock selectedSolutionsLock = new ReentrantLock();
	private AtomicInteger selectionTasksCounter = new AtomicInteger(0);
	private Double bestValidationScore = null;

	private Collection<Object> listeners = new ArrayList<>();
	private HASCO<C, TFDNode, String, Double, ForwardDecompositionSolution>.HASCOSolutionIterator hascoRun;
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator;
	private Queue<HASCOMLContinuousSelectionSolution<C>> solutionsFoundByHASCO = new PriorityQueue<>(
			new Comparator<HASCOMLContinuousSelectionSolution<C>>() {
				@Override
				public int compare(final HASCOMLContinuousSelectionSolution<C> o1,
						final HASCOMLContinuousSelectionSolution<C> o2) {
					return (int) Math.round(10000 * (o1.getValidationScore() - o2.getValidationScore()));
				}
			});
	private Queue<HASCOMLContinuousSelectionSolution<C>> solutionsSelectedByHASCO = new PriorityQueue<>(
			new Comparator<HASCOMLContinuousSelectionSolution<C>>() {
				@Override
				public int compare(final HASCOMLContinuousSelectionSolution<C> o1,
						final HASCOMLContinuousSelectionSolution<C> o2) {
					return (int) Math.round(10000 * (o1.getSelectionScore() - o2.getSelectionScore()));
				}
			});
	private BlockingQueue<Runnable> taskQueue = new PriorityBlockingQueue<>(
			(int) (this.numberOfConsideredSolutions * 1.5), new Comparator<Runnable>() {
				@SuppressWarnings("unchecked")
				@Override
				public int compare(final Runnable o1, final Runnable o2) {
					if (o1 instanceof HASCOMLContinuousSelection.SelectionPhaseEval
							&& o2 instanceof HASCOMLContinuousSelection.SelectionPhaseEval) {
						SelectionPhaseEval spe1 = (HASCOMLContinuousSelection<C>.SelectionPhaseEval) o1;
						SelectionPhaseEval spe2 = (HASCOMLContinuousSelection<C>.SelectionPhaseEval) o2;
						return spe1.solution.getValidationScore().compareTo(spe2.solution.getValidationScore());
					}
					return 0;
				}
			});

	/* derive existing components */
	private final ComponentLoader cl = new ComponentLoader();
	private ExecutorService threadPool;

	/** Benchmarks for estimating a candidate's quality */
	private IObjectEvaluator<C, Double> searchBenchmark;
	private IObjectEvaluator<C, Double> selectBenchmark;
	private IObjectEvaluator<C, Double> testBenchmark;

	public HASCOMLContinuousSelection(final File componentFile, final String requestedInterface, final int timeout,
			final long seed) throws IOException {
		this.cl.loadComponents(componentFile);
		this.requestedInterface = requestedInterface;
		this.timeout = timeout;
		this.seed = seed;
	}

	public void gatherSolutions(final int timeoutInMS, final Factory<C> converter) {
		if (this.isCanceled) {
			throw new IllegalStateException("HASCO has already been canceled. Cannot gather results anymore.");
		}

		// boot thread pool for selection phase
		this.threadPool = new ThreadPoolExecutor(this.numberOfSelectionCPUs, this.numberOfSelectionCPUs, 120,
				TimeUnit.SECONDS, this.taskQueue);

		long start = System.currentTimeMillis();
		long deadline = start + timeoutInMS;

		/* create algorithm */
		if (this.preferredNodeEvaluator == null) {
			try {
				this.preferredNodeEvaluator = new PreferenceBasedNodeEvaluator(this.cl.getComponents(),
						FileUtil.readFileAsList("model/combined/preferredNodeEvaluator.txt"));
			} catch (IOException e) {
				this.logger.error("Problem loading the preference-based node evaluator. Details:\n{}",
						LoggerUtil.getExceptionInfo(e));
				return;
			}
		}
		HASCOFD<C, Double> hasco = new HASCOFD<>(cl.getComponents(), cl.getParamConfigs(), converter, this.requestedInterface,
				this.searchBenchmark);
		hasco.setPreferredNodeEvaluator(preferredNodeEvaluator);
		hasco.setNumberOfCPUs(this.numberOfSearchCPUs);
		hasco.setTimeout(this.timeout);
		hasco.setLoggerName(this.loggerName + ".hasco");

		new SimpleGraphVisualizationWindow<Node<TFDNode, Double>>(hasco).getPanel()
				.setTooltipGenerator(new TFDTooltipGenerator<>());

		/* add all listeners to HASCO */
		this.listeners.forEach(l -> hasco.registerListener(l));
		this.listeners.forEach(l -> hasco.registerListenerForSolutionEvaluations(l));

		/* run HASCO */
		this.hascoRun = hasco.iterator();

		Random rand = new Random(this.seed);
		boolean deadlineReached = false;

		while (!this.isCanceled && this.hascoRun.hasNext()
				&& (timeoutInMS <= 0 || !(deadlineReached = System.currentTimeMillis() >= deadline))) {
			HASCOMLContinuousSelectionSolution<C> nextSolution = new HASCOMLContinuousSelectionSolution<>(
					this.hascoRun.next());
			/*
			 * Skip returned solutions that obtained a timeout or were not able to be
			 * computed
			 */
			if (nextSolution.getValidationScore() >= 10000) {
				continue;
			}
			this.logger.info("Solution found " + nextSolution.getSolution().toString() + " "
					+ nextSolution.getValidationScore());
			this.solutionsFoundByHASCO.add(nextSolution);
			List<HASCOMLContinuousSelectionSolution<C>> solutionList = new LinkedList<>(this.solutionsFoundByHASCO);

			if (this.bestValidationScore == null || nextSolution.getValidationScore() < this.bestValidationScore) {
				this.bestValidationScore = nextSolution.getValidationScore();
			}

			double coinFlipRatio = 1;
			if (this.selectionTasksCounter.get() > 10) {
				coinFlipRatio = (double) 10 / this.selectionTasksCounter.get();
			}

			double randomRatio = rand.nextDouble();

			boolean coinFlip = randomRatio <= coinFlipRatio;
			if (solutionList.indexOf(nextSolution) <= (this.numberOfConsideredSolutions / 2)
					|| (nextSolution.getValidationScore() < this.bestValidationScore + this.EPSILON && coinFlip)) {
				int tasks = this.selectionTasksCounter.incrementAndGet();
				this.threadPool.submit(new SelectionPhaseEval(nextSolution));
				this.logger.debug("Selection tasks in Queue: " + tasks + " (Submitted new task)");
			}

		}

		if (deadlineReached) {
			this.logger.info("Deadline has been reached");
		} else if (this.isCanceled) {
			this.logger.info("Interrupting HASCO due to cancel.");
		}
	}

	public void cancel() {
		this.isCanceled = true;
		if (this.hascoRun != null) {
			this.hascoRun.cancel();
		}
	}

	public Queue<HASCOMLContinuousSelectionSolution<C>> getFoundClassifiers() {
		return new LinkedList<>(this.solutionsFoundByHASCO);
	}

	public Queue<HASCOMLContinuousSelectionSolution<C>> getSelectedClassifiers() {
		return new LinkedList<>(this.solutionsSelectedByHASCO);
	}

	public HASCOMLContinuousSelectionSolution<C> getCurrentBestSolution() {
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

	public int getNumberOfSearchCPUs() {
		return this.numberOfSearchCPUs;
	}

	public void setNumberOfSearchCPUs(final int numberOfSearchCPUs) {
		this.numberOfSearchCPUs = numberOfSearchCPUs;
	}

	public int getNumberOfSelectionCPUs() {
		return this.numberOfSelectionCPUs;
	}

	public void setNumberOfSelectionCPUs(final int numberOfSelectionCPUs) {
		this.numberOfSelectionCPUs = numberOfSelectionCPUs;
	}

	public int getTimeout() {
		return this.timeout;
	}

	public void setTimeout(final int timeout) {
		this.timeout = timeout;
	}

	public void setSearchBenchmark(final IObjectEvaluator<C, Double> searchBenchmark) {
		this.searchBenchmark = searchBenchmark;
	}

	public void setSelectionBenchmark(final IObjectEvaluator<C, Double> selectBenchmark) {
		this.selectBenchmark = selectBenchmark;
	}

	public void setTestBenchmark(final IObjectEvaluator<C, Double> testBenchmark) {
		this.testBenchmark = testBenchmark;
	}

	public IObjectEvaluator<C, Double> getSearchBenchmark() {
		return this.searchBenchmark;
	}

	public IObjectEvaluator<C, Double> getSelectBenchmark() {
		return this.selectBenchmark;
	}

	public IObjectEvaluator<C, Double> getTestBenchmark() {
		return this.testBenchmark;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}
}
