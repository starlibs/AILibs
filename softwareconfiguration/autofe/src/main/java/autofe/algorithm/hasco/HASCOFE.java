package autofe.algorithm.hasco;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.configuration.AbstractConfiguration;
import hasco.core.HASCOFD;
import hasco.core.Solution;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.core.INodeEvaluator;

public class HASCOFE {

	/* logging */
	private Logger logger = LoggerFactory.getLogger(HASCOFE.class);

	private AbstractConfiguration config;
	private HASCOFD<IFilter>.HASCOSolutionIterator hascoRun;
	private INodeEvaluator<TFDNode, Double> nodeEvaluator;

	// Utility variables
	private boolean isCanceled = false;
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
		// TODO: Interface name
		// TODO: Benchmark
		// TODO: Weka Pipeline new WEKAPipelineFactory()
		HASCOFD<IFilter> hasco = new HASCOFD<>(null, this.nodeEvaluator, "FilterInterface", null);

		// TODO: Configure HASCO (Inject filters, listeners, ...)

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

}
