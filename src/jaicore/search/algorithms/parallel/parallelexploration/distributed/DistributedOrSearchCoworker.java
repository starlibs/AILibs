package jaicore.search.algorithms.parallel.parallelexploration.distributed;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.interfaces.IORGraphSearchFactory;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.algorithms.parallel.parallelevaluation.local.core.ParallelizedORGraphSearch;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.DistributedSearchCommunicationLayer;
import jaicore.search.algorithms.standard.core.NodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;

public class DistributedOrSearchCoworker<T, A, V extends Comparable<V>> {

	private final static Logger logger = LoggerFactory.getLogger(DistributedOrSearchCoworker.class);

	private final IORGraphSearchFactory<T, A, V> algorithmFactory;
	private final DistributedSearchCommunicationLayer<T, A, V> coworkerInterface;
	protected final String id;
	private final int searchTime;
	private final int uptime;
	private boolean shutdown = false;

	public DistributedOrSearchCoworker(IORGraphSearchFactory<T, A, V> algorithmFactory, DistributedSearchCommunicationLayer<T, A, V> coworkerInterface, String id, int uptime, int searchTime) {
		super();
		this.algorithmFactory = algorithmFactory;
		this.coworkerInterface = coworkerInterface;
		this.id = id;
		this.searchTime = searchTime;
		this.uptime = uptime;
		logger.info("Created new coworker {}", this.id);
	}

	public void cowork() {
		
		final Thread runningThread = Thread.currentThread();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				logger.info("Shutting down coworker " + id + ". Note that the shutdown will be effective not until the current job is finished.");
				shutdown = true;
				
				/* if we have not been attached, we stop working */
				if (!coworkerInterface.isAttached(id)) {
					logger.info("Coworker has not been attached, so interrupting waiting thread.");
					runningThread.interrupt();
				}
				coworkerInterface.unregister(id);
			}
		}, uptime);

		/* register this coworker and setup the timer for unregister */
		try {
			coworkerInterface.register(this.id);
			logger.info("Coworker has been attached.");
		}
		catch (InterruptedException e) {
			logger.info("Coworker was interrupted while waiting for attachment.");
		}
		
		/* now busily wait for jobs forever */
		GraphGenerator<T, A> graphGenerator = null;
		NodeEvaluator<T, V> nodeEvaluator = null;
		try {
			while (!shutdown || coworkerInterface.isAttached(this.id)) {

				/* wait until a new job has arrived */
				logger.info("Waiting for next job ...");
				final Collection<Node<T, V>> nodes = coworkerInterface.nextJob(this.id);
				logger.info("Found new job ...");
				if (nodes == null || nodes.isEmpty()) {
					logger.warn("Received NULL or EMPTY node list.");
					break;
				}

				/* if this is the first job, get graph generator and node evaluator */
				if (graphGenerator == null || nodeEvaluator == null) {
					graphGenerator = coworkerInterface.getGraphGenerator();
					nodeEvaluator = coworkerInterface.getNodeEvaluator();
				}

				/* if we became detached and want to shut down, leave loop */
				if (shutdown && !coworkerInterface.isAttached(this.id))
					break;

				/* setup the search algorithm with the graph generator, and configure a time out */
				ORGraphSearch<T, A, V> searchAlgorithm = (ORGraphSearch<T, A, V>) algorithmFactory.getSearch(graphGenerator, nodeEvaluator);
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						searchAlgorithm.cancel();
					}
				}, searchTime);

				/* run the algorithm */
//				new SimpleGraphVisualizationWindow<>(((IObservableORGraphSearch<T, A, V>)searchAlgorithm).getEventBus());
				List<T> solution;
				List<Node<T, V>> solutionNodes = new ArrayList<>();
				logger.info("Running coworker {} with: {}", this.id, nodes.stream().map(n -> n.getPoint()).collect(Collectors.toList()));
				searchAlgorithm.bootstrap(nodes);
				do {
					solution = searchAlgorithm.nextSolution();
					if (solution != null)
						solutionNodes.add(searchAlgorithm.getInternalRepresentationOf(solution.get(solution.size() - 1)));
				} while (solution != null);
				logger.info("Coworker {} finished, reporting results and going to wait for new jobs.", this.id);

				/* report results */
				Collection<Node<T, V>> openNodes = searchAlgorithm.getOpenSnapshot();
				logger.info("Reporting open list of size " + openNodes.size() + " and " + solutionNodes.size() + " solutions.");
				DistributedComputationResult<T, V> result = new DistributedComputationResult<>(this.id, openNodes, solutionNodes);
				coworkerInterface.reportResult(this.id, result);
			}
		} catch (InterruptedException e) {
			logger.info("Received interrupt.");
		} catch (NoClassDefFoundError | ClassNotFoundException e) {
			logger.error("Cannot perform the search as the class {} was not found on the classpath. This is probably a problem of serialization. Simply make sure that the class is on the classpath for the coworker.", e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		/* shutdown infrastructure */
		timer.cancel();
		if (coworkerInterface.isAttached(id))
			coworkerInterface.detachCoworker(id);
		else
			coworkerInterface.unregister(id);
		coworkerInterface.close();
		logger.info("Terminating.");
	}

	public static <T,A,V extends Comparable<V>> void main(String[] args) {
		
		if (args.length < 4) {
			System.err.println("Need at least 4 args: communicationFolder, coworkerId, searchTime, upTime[, numThreads]");
			System.exit(1);
		}
		
		Path folder = Paths.get(args[0]);
		String id = args[1];
		int searchTime = Integer.parseInt(args[2]) * 1000;
		int uptime = Integer.parseInt(args[3]) * 1000;
		int threads = (args.length > 4) ? Integer.parseInt(args[4]) : 1;
		
		logger.info("Using {} threads.", threads);
		
		IORGraphSearchFactory<T, A, V> factory = (threads == 1 ? (gen, eval) -> new jaicore.search.algorithms.standard.core.ORGraphSearch<>(gen, eval) : (gen, eval) -> new ParallelizedORGraphSearch<>(gen, eval, threads, 1000));
		DistributedSearchCommunicationLayer<T, A, V> communicationLayer = new FolderBasedDistributedSearchCommunicationLayer<>(folder, false);
		new DistributedOrSearchCoworker<>(factory, communicationLayer, id, uptime, searchTime).cowork();
	}
}
