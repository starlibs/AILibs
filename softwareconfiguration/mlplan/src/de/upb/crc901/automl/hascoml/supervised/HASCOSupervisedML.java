package de.upb.crc901.automl.hascoml.supervised;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.core.HASCOFD;
import hasco.core.Solution;
import hasco.query.Factory;
import hasco.serialization.ComponentLoader;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.TimeOut;
import jaicore.graph.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.ml.evaluation.TimeoutableEvaluator;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.search.algorithms.standard.uncertainty.OversearchAvoidanceConfig;
import jaicore.search.structure.core.Node;

/**
 * HASCOML represents the basic class for searching and optimizing hierarchical algorithm selection and configuration problems specifically for machine learning.
 */
public class HASCOSupervisedML<V> extends HASCOFD<V, Double> implements IObservableGraphAlgorithm<TFDNode, String>, ILoggingCustomizable {

	/** HASCO parametrization specific for the use case of supervised ML. */
	private static final HASCOSupervisedMLConfig CONFIG = ConfigCache.getOrCreate(HASCOSupervisedMLConfig.class);

	/** The name of the requested interface. */
	public static String REQUESTED_INTERFACE = "AbstractClassifier";

	/** Logger for controlled output */
	private Logger logger = LoggerFactory.getLogger(HASCOSupervisedML.class);

	/** Logger name that can be used to customize logging outputs in a more convenient way. */
	private String loggerName;

	/**
	 * Namespaced class for storing evaluated classifiers that have been found by HASCO.
	 */
	public static class HASCOClassificationMLSolution<V> extends Solution<ForwardDecompositionSolution, V, Double> {
		public HASCOClassificationMLSolution(final Solution<ForwardDecompositionSolution, V, Double> solution) {
			super(solution);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.getSolution().toString());
			sb.append("\n");
			sb.append("Time to compute score: " + this.getTimeToComputeScore() + "ms");
			sb.append("\n");
			sb.append("Solution score: " + this.getScore());
			return sb.toString();
		}
	}

	/** Flag whether the process has been canceled via external call. */
	private boolean isCanceled = false;

	/**
	 * The iterator instance for actually running hasco.
	 */
	private HASCOFD<V, Double>.HASCOSolutionIterator hascoRun;

	/**
	 * Timeout for single node evaluation in seconds. -1 denotes no timeout at all.
	 */
	private int timeoutNodeEvaluationInS = -1;

	/** Flag whether to enable visualization. */
	private boolean enableVisualization = false;

	/** Instance of the component loader providing the collection of components and the detailed parameter refinement configuration. */
	private ComponentLoader componentLoader;

	/**
	 * Queue containing all solutions that have been found by hasco ordered according to their (internal) score.
	 */
	private Queue<HASCOClassificationMLSolution<V>> solutionsFoundByHASCO = new PriorityQueue<>(new Comparator<HASCOClassificationMLSolution<V>>() {
		@Override
		public int compare(final HASCOClassificationMLSolution<V> o1, final HASCOClassificationMLSolution<V> o2) {
			return o1.getScore().compareTo(o2.getScore());
		}
	});

	/**
	 * C'tor requiring a component loader. For actually starting to gather solutions, it is mandatory to also set the factory and evaluator afterwards.
	 *
	 * @param componentLoader
	 *            A component loader to get the description of the software configuration problem.
	 * @throws IOException
	 *             Throws an IOException if the components could not be loaded with the given configuration file.
	 */
	public HASCOSupervisedML(final ComponentLoader componentLoader) throws IOException {
		this(componentLoader, null, null, new OversearchAvoidanceConfig<>());
	}

	/**
	 * C'tor requiring a component loader. For actually starting to gather solutions, it is mandatory to also set the factory and evaluator afterwards.
	 *
	 * @param componentLoader
	 *            A component loader to get the description of the software configuration problem.
	 * @param factory
	 *            Factory to transform a plan into a concrete (executable) object.
	 * @param evaluator
	 *            Evaluator for assessing the quality of a solution node.
	 * @param oversearchAvoidanceConfig
	 *            Configuration to determine how to avoid oversearch.
	 * @throws IOException
	 *             Throws an IOException if the components could not be loaded with the given configuration file.
	 */
	public HASCOSupervisedML(final ComponentLoader componentLoader, final Factory<V> factory, final IObjectEvaluator<V, Double> evaluator, final OversearchAvoidanceConfig<TFDNode, Double> oversearchAvoidanceConfig) throws IOException {
		super(componentLoader.getComponents(), componentLoader.getParamConfigs(), factory, REQUESTED_INTERFACE, evaluator, oversearchAvoidanceConfig);
		this.setRequestedInterface(this.getConfig().requestedInterface());
	}

	/**
	 * Gather solutions for some data within a given timeout.
	 *
	 * @param timeoutInMS
	 * @throws IOException
	 */
	public void gatherSolutions(final TimeOut timeout) throws IOException {
		if (this.isCanceled) {
			throw new IllegalStateException("HASCO has already been canceled. Cannot gather results anymore.");
		}

		/* Check whether a classifier evaluator has been set. */
		if (this.getBenchmark() == null) {
			throw new IllegalArgumentException("A classifier evaluator has to be set before solutions can be gathered.");
		}

		/* Check whether a classifier factory has been set. */
		if (this.getFactory() == null) {
			throw new IllegalArgumentException("A classifier factory has to be set before solutions can be gathered.");
		}

		this.logger.info("Starting to gather solutions for {}ms.", timeout.milliseconds());
		long start = System.currentTimeMillis();
		long deadline = start + timeout.milliseconds();

		/* Load components */
		this.logger.debug("Loading components ...");

		/*
		 * Check whether a timeout has been defined for a single node evaluation and if so wrap the classifier evaluator in a timeoutable evaluator.
		 */
		if (this.timeoutNodeEvaluationInS > 0) {
			if (!(this.getSolutionEvaluator() instanceof TimeoutableEvaluator)) {
				super.setBenchmark(new TimeoutableEvaluator<V>(this.getBenchmark(), this.timeoutNodeEvaluationInS * 1000));
			}
		}

		/* create algorithm */
		if (this.enableVisualization) {
			new SimpleGraphVisualizationWindow<Node<TFDNode, Double>>(this).getPanel().setTooltipGenerator(new TFDTooltipGenerator<>());
		}

		if (this.loggerName != null && this.loggerName.length() > 0) {
			this.setLoggerName(this.loggerName + ".hasco");
		}

		/* run HASCO */
		this.hascoRun = super.iterator();
		boolean deadlineReached = false;

		this.logger.info("Entering loop ...");
		while (!this.isCanceled && this.hascoRun.hasNext() && (timeout.milliseconds() <= 0 || !(deadlineReached = System.currentTimeMillis() >= deadline))) {
			HASCOClassificationMLSolution<V> nextSolution = new HASCOClassificationMLSolution<V>(this.hascoRun.next());
			this.solutionsFoundByHASCO.add(nextSolution);
		}

		if (deadlineReached) {
			this.logger.info("Deadline has been reached");
		} else if (this.isCanceled) {
			this.logger.info("Interrupting HASCO due to cancel.");
		} else {
			this.logger.info("HASCO finished.");
		}
		this.cancel();
	}

	/**
	 * Cancel the execution of this hasco run.
	 */
	public void cancel() {
		this.isCanceled = true;
		if (this.hascoRun != null) {
			this.hascoRun.cancel();
		}
	}

	/**
	 * @return Returns a sorted queue of solutions with respect to the internal validation score.
	 */
	public Queue<HASCOClassificationMLSolution<V>> getFoundClassifiers() {
		return new LinkedList<>(this.solutionsFoundByHASCO);
	}

	/**
	 * @return Returns the solution that is best with respect to the internal validation score.
	 */
	public HASCOClassificationMLSolution<V> getCurrentlyBestSolution() {
		return this.solutionsFoundByHASCO.peek();
	}

	@Override
	public HASCOSupervisedMLConfig getConfig() {
		return CONFIG;
	}

	public void setTimeoutForSingleFEvaluation(final int timeoutForSingleFEvaluation) {
		this.timeoutNodeEvaluationInS = timeoutForSingleFEvaluation;
	}

	public int getTimeoutForSingleFEvaluation() {
		return this.timeoutNodeEvaluationInS;
	}

	/**
	 * Enable or disable the search graph visualization.
	 *
	 * @param isEnabled
	 *            Flag whether the search graph visualization shall be enabled. True: Enabled; False: Disabled.
	 */
	public void enableVisualization(final boolean isEnabled) {
		this.enableVisualization = isEnabled;
	}

	/**
	 * @return The instance of the component loader.
	 */
	public ComponentLoader getComponentLoader() {
		return this.componentLoader;
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
