package autofe.algorithm.hasco;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.evaluation.AbstractHASCOFENodeEvaluator;
import autofe.algorithm.hasco.evaluation.AbstractHASCOFEObjectEvaluator;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.algorithm.hasco.filter.meta.FilterPipelineFactory;
import autofe.util.DataSet;
import autofe.util.FilterUtils;
import hasco.core.HASCOFD;
import hasco.core.Solution;
import hasco.model.Component;
import hasco.serialization.ComponentLoader;
import jaicore.basic.ILoggingCustomizable;
import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.search.structure.core.Node;

/**
 * HASCO Feature Engineering class executing a HASCO run using
 * <code>FilterPipeline</code> objects.
 * 
 * @author Julian Lienen
 *
 */
public class HASCOFE implements IObservableGraphAlgorithm<TFDNode, String>, ILoggingCustomizable {

	// Search relevant properties
	private File configFile;
	private HASCOFD<FilterPipeline> hasco;
	private HASCOFD<FilterPipeline>.HASCOSolutionIterator hascoRun;
	// private INodeEvaluator<TFDNode, Double> nodeEvaluator;

	// Logging
	private Logger logger = LoggerFactory.getLogger(HASCOFE.class);
	private String loggerName;

	// Utility variables
	private int timeoutInS;
	private long timeOfStart = -1;
	private boolean isCanceled = false;
	private Collection<Object> listeners = new ArrayList<>();
	private Queue<HASCOFESolution> solutionsFoundByHASCO = new PriorityQueue<>(new Comparator<HASCOFESolution>() {

		public int compare(final HASCOFESolution o1, final HASCOFESolution o2) {
			return o1.getScore().compareTo(o2.getScore());
		}
	});

	public static class HASCOFESolution extends Solution<ForwardDecompositionSolution, FilterPipeline, Double> {
		public HASCOFESolution(Solution<ForwardDecompositionSolution, FilterPipeline, Double> solution) {
			super(solution);
		}

		@Override
		public String toString() {
			return "HASCOFESolution [getSolution()=" + getSolution() + "]";
		}
	}

	public HASCOFE(final File config, AbstractHASCOFENodeEvaluator nodeEvaluator, final DataSet data,
			AbstractHASCOFEObjectEvaluator benchmark) {

		if (config == null || !config.exists())
			throw new IllegalArgumentException(
					"The file " + config + " is null or does not exist and cannot be used by ML-Plan");

		this.configFile = config;
		this.initializeHASCOSearch(data, nodeEvaluator, benchmark);
	}

	private void initializeHASCOSearch(final DataSet data, AbstractHASCOFENodeEvaluator nodeEvaluator,
			AbstractHASCOFEObjectEvaluator benchmark) { // AbstractHASCOFEObjectEvaluator
		// benchmark
		benchmark.setData(data);
		if (nodeEvaluator != null) {
			nodeEvaluator.setHascoFE(this);
			nodeEvaluator.setData(data);

			this.hasco = new HASCOFD<>(new FilterPipelineFactory(), nodeEvaluator, "FilterPipeline", benchmark);
		} else {
			this.hasco = new HASCOFD<>(new FilterPipelineFactory(), n -> null, "FilterPipeline", benchmark);
		}

		// TODO
		// this.hasco.setNumberOfCPUs(4);
		if (this.loggerName != null && this.loggerName.length() > 0)
			this.hasco.setLoggerName(loggerName + ".hasco");

		try {
			ComponentLoader cl = new ComponentLoader();
			cl.loadComponents(this.configFile);
			this.hasco.addComponents(cl.getComponents());
			this.hasco.addParamRefinementConfigurations(cl.getParamConfigs());
		} catch (IOException e) {
			logger.warn("Could not import configuration file. Using default components instead...");
			e.printStackTrace();
			final List<Component> components = FilterUtils.getDefaultComponents();
			this.hasco.addComponents(components);
		}

		// Add listeners
		this.listeners.forEach(l -> this.hasco.registerListener(l));

		// Set run iterator used for search
		this.hascoRun = this.hasco.iterator();
	}

	public void runSearch(final int timeoutInMS) {

		long start = System.currentTimeMillis();
		long deadline = start + timeoutInMS;
		this.timeOfStart = System.currentTimeMillis();
		this.timeoutInS = timeoutInMS / 1000;

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (!Thread.interrupted()) {
						Thread.sleep(100);
						int timeElapsed = (int) (System.currentTimeMillis() - HASCOFE.this.timeOfStart);
						int timeRemaining = HASCOFE.this.timeoutInS * 1000 - timeElapsed;

						// TODO
						if (timeRemaining < 0) {
							logger.info("Cancelling search...");
							HASCOFE.this.cancel();
							return;
						}
					}
				} catch (InterruptedException e) {
				}

			}
		}, "Phase 1 time bound observer").start();

		boolean deadlineReached = false;
		while (!this.isCanceled && this.hascoRun.hasNext()
				&& (timeoutInMS <= 0 || !(deadlineReached = System.currentTimeMillis() >= deadline))) {
			HASCOFESolution nextSolution = new HASCOFESolution(this.hascoRun.next());
			this.solutionsFoundByHASCO.add(nextSolution);
		}
		if (deadlineReached) {
			logger.info("Deadline has been reached.");
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

	public Queue<HASCOFESolution> getFoundClassifiers() {
		return new LinkedList<>(this.solutionsFoundByHASCO);
	}

	public HASCOFESolution getCurrentlyBestSolution() {
		return this.solutionsFoundByHASCO.peek();
	}

	@Override
	public void setLoggerName(String name) {
		logger.info("Switching logger from {} to {}", logger.getName(), name);
		this.loggerName = name;
		logger = LoggerFactory.getLogger(name);
		logger.info("Activated logger {} with name {}", name, logger.getName());
	}

	@Override
	public String getLoggerName() {
		return loggerName;
	}

	@Override
	public void registerListener(Object listener) {
		this.listeners.add(listener);
	}

	public void enableVisualization() {
		if (this.timeOfStart >= 0)
			throw new IllegalStateException(
					"Cannot enable visualization after buildClassifier has been invoked. Please enable it previously.");

		new SimpleGraphVisualizationWindow<Node<TFDNode, Double>>(this).getPanel()
				.setTooltipGenerator(new TFDTooltipGenerator<>());
	}

	public HASCOFD<FilterPipeline> getHasco() {
		return hasco;
	}
}
