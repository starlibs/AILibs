package de.upb.crc901.automl.hascowekaml;

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

import de.upb.crc901.automl.pipeline.ClassifierFactory;
import hasco.core.HASCOFD;
import hasco.core.Solution;
import hasco.model.Component;
import hasco.serialization.ComponentLoader;
import jaicore.basic.ILoggingCustomizable;
import jaicore.graph.IObservableGraphAlgorithm;
import jaicore.ml.evaluation.ClassifierEvaluator;
import jaicore.ml.evaluation.TimeoutableEvaluator;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.interfaces.ISolutionEvaluator;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.uncertainty.OversearchAvoidanceConfig;
import jaicore.search.structure.core.GraphGenerator;
import weka.classifiers.Classifier;

/**
 * HASCOML represents the basic class for searching and optimizing hierarchical
 * algorithm selection and configuration problems specifically for machine
 * learning.
 */
public class HASCOClassificationML implements IObservableGraphAlgorithm<TFDNode, String>, ILoggingCustomizable {
	/** Logger for controlled output */
	private Logger logger = LoggerFactory.getLogger(HASCOClassificationML.class);
	private String loggerName;

	private static final String REQUEST_INTERFACE = "Classifier";

	/**
	 * Namespaced class for storing evaluated classifiers that have been found by
	 * HASCO.
	 */
	public static class HASCOClassificationMLSolution
			extends Solution<ForwardDecompositionSolution, Classifier, Double> {
		public HASCOClassificationMLSolution(
				final Solution<ForwardDecompositionSolution, Classifier, Double> solution) {
			super(solution);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.getSolution().toString());
			return sb.toString();
		}
	}

	// FIXME: this might be rather a hyperparameter than something hardcoded
	private OversearchAvoidanceConfig<TFDNode> oversearchAvoidanceConfig = new OversearchAvoidanceConfig<>(
			OversearchAvoidanceConfig.OversearchAvoidanceMode.NONE);

	/** Flag whether the process has been canceled via external call. */
	private boolean isCanceled = false;

	/** Collection of listeners to hand out updates of the process. */
	private Collection<Object> listeners = new ArrayList<>();

	/**
	 * The hasco instance for the respective problem.
	 */
	private HASCOFD<Classifier, Double> hasco;

	/**
	 * The iterator instance for actually running hasco.
	 */
	private HASCOFD<Classifier, Double>.HASCOSolutionIterator hascoRun;

	/**
	 * This is a pointer to the configuration file defining the components for
	 * HASCO.
	 */
	private final File componentConfigurationFile;

	/**
	 * The preferred node evaluator
	 */
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator = (node) -> null;

	/**
	 * The classifier factory compile composition instances to classifier objects.
	 */
	private ClassifierFactory classifierFactory = null;

	/**
	 * The actual node evaluator executing nodes and estimating their performance.
	 */
	private ClassifierEvaluator classifierEvaluator = null;

	/**
	 * Timeout for single node evaluation in seconds. -1 denotes no timeout at all.
	 */
	private int timeoutNodeEvaluationInS = -1;

	/**
	 * Queue containing all solutions that have been found by hasco ordered
	 * according to their (internal) score.
	 */
	private Queue<HASCOClassificationMLSolution> solutionsFoundByHASCO = new PriorityQueue<>(
			new Comparator<HASCOClassificationMLSolution>() {
				@Override
				public int compare(final HASCOClassificationMLSolution o1, final HASCOClassificationMLSolution o2) {
					return o1.getScore().compareTo(o2.getScore());
				}
			});

	/**
	 * C'tor requiring a component configuration file.
	 *
	 * @param hascoConfigurationFile
	 *            A file describing the components available to HASCO.
	 */
	public HASCOClassificationML(final File hascoConfigurationFile) {
		this.componentConfigurationFile = hascoConfigurationFile;
	}

	/**
	 * @return Returns the defined preferred node evaluator.
	 */
	public INodeEvaluator<TFDNode, Double> getPreferredNodeEvaluator() {
		return this.preferredNodeEvaluator;
	}

	/**
	 * Configure HASCO with a preferred node evaluator, e.g. first perform a
	 * breadth-first search.
	 *
	 * @param preferredNodeEvaluator
	 *            The preferred node evaluator to use by HASCO.
	 */
	public void setPreferredNodeEvaluator(final INodeEvaluator<TFDNode, Double> preferredNodeEvaluator) {
		this.preferredNodeEvaluator = preferredNodeEvaluator;
	}

	/**
	 * @return Returns the classifier factory, compile component instances to
	 *         classifiers, that is to be used by HASCO.
	 */
	public ClassifierFactory getClassifierFactory() {
		return this.classifierFactory;
	}

	/**
	 * @param classifierFactory
	 *            The classifier factory to use by HASCO.
	 */
	public void setClassifierFactory(final ClassifierFactory classifierFactory) {
		this.classifierFactory = classifierFactory;
	}

	/**
	 * @return Returns the classifier evaluator that is to be used by HASCO.
	 */
	public ClassifierEvaluator getClassifierEvaluator() {
		return this.classifierEvaluator;
	}

	/**
	 * @param classifierEvaluator
	 *            The classifier evaluator that shall be used by HASCO.
	 */
	public void setClassifierEvaluator(final ClassifierEvaluator classifierEvaluator) {
		this.classifierEvaluator = classifierEvaluator;
	}

	/**
	 * Gather solutions for some data within a given timeout.
	 *
	 * @param timeoutInMS
	 * @throws IOException
	 */
	public void gatherSolutions(final int timeoutInMS) throws IOException {
		if (this.isCanceled) {
			throw new IllegalStateException("HASCO has already been canceled. Cannot gather results anymore.");
		}

		/* Check whether a classifier evaluator has been set. */
		if (this.classifierEvaluator == null) {
			throw new IllegalArgumentException(
					"A classifier evaluator has to be set before solutions can be gathered.");
		}

		/* Check whether a classifier factory has been set. */
		if (this.classifierFactory == null) {
			throw new IllegalArgumentException("A classifier factory has to be set before solutions can be gathered.");
		}

		this.logger.info("Starting to gather solutions for {}ms.", timeoutInMS);
		long start = System.currentTimeMillis();
		long deadline = start + timeoutInMS;

		/* Load components */
		this.logger.debug("Loading components ...");
		ComponentLoader cl = new ComponentLoader();
		cl.loadComponents(this.componentConfigurationFile);

		/*
		 * Check whether a timeout has been defined for a single node evaluation and if
		 * so wrap the classifier evaluator in a timeoutable evaluator.
		 */
		if (this.timeoutNodeEvaluationInS > 0) {
			if (!(this.classifierEvaluator instanceof TimeoutableEvaluator)) {
				this.classifierEvaluator = new TimeoutableEvaluator(this.classifierEvaluator,
						this.timeoutNodeEvaluationInS * 1000);
			}
		}

		/* create algorithm */
		this.hasco = new HASCOFD<>(cl.getComponents(), cl.getParamConfigs(), this.classifierFactory, REQUEST_INTERFACE,
				this.classifierEvaluator, this.oversearchAvoidanceConfig);
		this.hasco.setPreferredNodeEvaluator(this.preferredNodeEvaluator);

		if (this.loggerName != null && this.loggerName.length() > 0) {
			this.hasco.setLoggerName(this.loggerName + ".hasco");
		}

		/* add all listeners to HASCO */
		this.logger.info("Registering listeners ...");
		this.listeners.forEach(l -> this.hasco.registerListener(l));

		/* run HASCO */
		this.hascoRun = this.hasco.iterator();
		boolean deadlineReached = false;

		this.logger.info("Entering loop ...");
		while (!this.isCanceled && this.hascoRun.hasNext()
				&& (timeoutInMS <= 0 || !(deadlineReached = System.currentTimeMillis() >= deadline))) {
			HASCOClassificationMLSolution nextSolution = new HASCOClassificationMLSolution(this.hascoRun.next());
			this.solutionsFoundByHASCO.add(nextSolution);
		}

		if (deadlineReached) {
			this.logger.info("Deadline has been reached");
		} else if (this.isCanceled) {
			this.logger.info("Interrupting HASCO due to cancel.");
		} else {
			this.logger.info("HASCO finished.");
		}
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

	public Queue<HASCOClassificationMLSolution> getFoundClassifiers() {
		return new LinkedList<>(this.solutionsFoundByHASCO);
	}

	public HASCOClassificationMLSolution getCurrentlyBestSolution() {
		return this.solutionsFoundByHASCO.peek();
	}

	@Override
	public void registerListener(final Object listener) {
		this.listeners.add(listener);
	}

	public void setTimeoutForSingleFEvaluation(final int timeoutForSingleFEvaluation) {
		this.timeoutNodeEvaluationInS = timeoutForSingleFEvaluation;
	}

	public void setOversearchAvoidanceMode(final OversearchAvoidanceConfig oversearchAvoidanceConfig) {
		this.oversearchAvoidanceConfig = oversearchAvoidanceConfig;
	}

	public Collection<Component> getComponents() {
		if (this.hasco == null || this.hasco.getComponents() == null || this.hasco.getComponents().size() == 0) {
			return null;
		} else {
			return this.hasco.getComponents();
		}
	}

	public GraphGenerator<TFDNode, String> getGraphGenerator() {
		if (this.hasco == null) {
			throw new IllegalArgumentException(
					"HASCOForWEKAML does not produce the actual HASCO object prior to knowing the data, which are passed when invoking \"gatherSolutions\". This has apparently not happened yet, so I cannot tell the graph generator neither at this time.");
		}
		return this.hasco.getGraphGenerator();
	}

	public ISolutionEvaluator<TFDNode, Double> getSolutionEvaluator() {
		return this.hasco.getSolutionEvaluator();
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
