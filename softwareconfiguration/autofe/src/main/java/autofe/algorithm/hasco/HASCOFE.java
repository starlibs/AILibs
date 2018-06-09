package autofe.algorithm.hasco;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipelineFactory;
import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.configuration.AbstractConfiguration;
import hasco.core.HASCOFD;
import hasco.core.Solution;
import hasco.model.Component;
import jaicore.basic.ILoggingCustomizable;
import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.core.INodeEvaluator;

public class HASCOFE implements IObservableGraphAlgorithm<TFDNode, String>, ILoggingCustomizable {

	// Search relevant properties
	private AbstractConfiguration config;
	private HASCOFD<IFilter>.HASCOSolutionIterator hascoRun;
	private INodeEvaluator<TFDNode, Double> nodeEvaluator;

	// Logging
	private Logger logger = LoggerFactory.getLogger(HASCOFE.class);
	private String loggerName;

	// Utility variables
	private boolean isCanceled = false;
	private Collection<Object> listeners = new ArrayList<>();
	private Queue<HASCOFESolution> solutionsFoundByHASCO = new PriorityQueue<>(new Comparator<HASCOFESolution>() {

		public int compare(final HASCOFESolution o1, final HASCOFESolution o2) {
			return o1.getScore().compareTo(o2.getScore());
		}
	});

	public static class HASCOFESolution extends Solution<ForwardDecompositionSolution, IFilter, Double> {
		public HASCOFESolution(Solution<ForwardDecompositionSolution, IFilter, Double> solution) {
			super(solution);
		}
	}

	public HASCOFE(AbstractConfiguration config, INodeEvaluator<TFDNode, Double> nodeEvaluator) {
		this.config = config;
		this.nodeEvaluator = nodeEvaluator;
		this.initializeHASCOSearch();
	}

	private void initializeHASCOSearch() {

		// TODO: Insert benchmark (last parameter) for phase 2 if necessary
		HASCOFD<IFilter> hasco = new HASCOFD<>(new FilterPipelineFactory(), this.nodeEvaluator, "FilterPipeline", null);
		if (this.loggerName != null && this.loggerName.length() > 0)
			hasco.setLoggerName(loggerName + ".hasco");

		// TODO: Configure HASCO (Inject filters components)
		// TODO: Make this automated (i. e. use configuration)
		final List<Component> components = getDefaultComponents();
		hasco.addComponents(components);

		// Add listeners
		this.listeners.forEach(l -> hasco.registerListener(l));

		// Set run iterator used for search
		this.hascoRun = hasco.iterator();
	}

	public void runSearch(final int timeoutInMS) {

		long start = System.currentTimeMillis();
		long deadline = start + timeoutInMS;

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

	private static List<Component> getDefaultComponents() {
		final List<Component> components = new ArrayList<>();

		Component c = new Component("FilterXy");
		// c.addProvidedInterface("IFilter");
		components.add(c);

		Component c1 = new Component("FilterXyz");
		components.add(c1);

		return components;
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

}
