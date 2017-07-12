package jaicore.search.algorithms.parallel.parallelexploration.distributed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.DistributableGraphGenerator;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.DistributedSearchCommunicationLayer;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableRootGenerator;
import jaicore.search.algorithms.standard.core.NodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class DistributedOrSearchCoworker<T, A, V extends Comparable<V>> {

	private final static Logger logger = LoggerFactory.getLogger(DistributedOrSearchCoworker.class);

	private final DistributedSearchCommunicationLayer<T, A, V> coworkerInterface;
	private BootstrappedORGraphSearch<T, A, V> searchAlgorithm;
	protected final String id;
	private final int numberOfThreads;
	private final int searchTime;
	private final int uptime;
	private boolean shutdown = false;

	public DistributedOrSearchCoworker(DistributedSearchCommunicationLayer<T, A, V> coworkerInterface, String id, int uptime, int searchTime, int numberOfThreads) {
		super();
		this.coworkerInterface = coworkerInterface;
		this.id = id;
		this.searchTime = searchTime;
		this.uptime = uptime;
		this.numberOfThreads = numberOfThreads;
		logger.info("Created new coworker {}", this.id);
	}

	public void cowork() {

		/* register this coworker and setup the timer for unregister */
		coworkerInterface.register(this.id);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println("Shutting down coworker " + id + ". Note that the shutdown will be effective not until the current job is finished.");
				shutdown = true;
				coworkerInterface.unregister(id);
			}
		}, uptime);

		/* now busily wait for jobs forever */
		while (!shutdown || coworkerInterface.isAttached(this.id)) {

			/* wait until a new job has arrived */
			while (!coworkerInterface.hasNewJob(this.id)) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					shutdown = true;
					break;
				}
			}
			if (shutdown && !coworkerInterface.isAttached(this.id))
				break;

			/* if we have a new job, run the search algorithm on it */
			logger.info("Found new job ...");
			final Collection<Node<T, V>> nodes = coworkerInterface.getJobDescription(this.id);
			if (nodes == null || nodes.isEmpty()) {
				logger.warn("Received NULL or EMPTY node list.");
				break;
			}

			/* setup time out for the search process */
			logger.info("Running coworker {} with: {}", this.id, nodes.stream().map(n -> n.getPoint()).collect(Collectors.toList()));
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					searchAlgorithm.cancel();
				}
			}, searchTime);

			/* read in graph generator and node evaluator */
			try {
				final GraphGenerator<T, A> graphGenerator = coworkerInterface.getGraphGenerator();
				final NodeEvaluator<T, V> nodeEvaluator = coworkerInterface.getNodeEvaluator();

				/* setup the search algorithm with the graph generator */
				searchAlgorithm = new BootstrappedORGraphSearch<>(graphGenerator, nodeEvaluator, nodes);

				/* run the algorithm */
				new SimpleGraphVisualizationWindow<>(searchAlgorithm.getEventBus());
				List<T> solution;
				List<Node<T, V>> solutionNodes = new ArrayList<>();

				do {
					solution = searchAlgorithm.nextSolution();
					if (solution != null)
						solutionNodes.add(searchAlgorithm.getInternalRepresentationOf(solution.get(solution.size() - 1)));
				} while (solution != null);
				logger.info("Coworker {} finished, reporting results and going to wait for new jobs.", this.id);

				/* report results */
				Collection<List<Node<T, V>>> pathsToOpenNodes = searchAlgorithm.getOpenSnapshot().stream().map(n -> n.path()).collect(Collectors.toList());
				Collection<List<Node<T, V>>> pathsToGoalNodes = solutionNodes.stream().map(n -> n.path()).collect(Collectors.toList());
				logger.info("Reporting open list of size " + pathsToOpenNodes.size() + " and " + solutionNodes.size() + " solutions.");
				DistributedComputationResult<T, V> result = new DistributedComputationResult<>(this.id, pathsToOpenNodes, pathsToGoalNodes);
				coworkerInterface.reportResult(this.id, result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		timer.cancel();
		System.out.println("Terminating");
	}
}
