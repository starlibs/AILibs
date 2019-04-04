//package jaicore.search.algorithms.parallel.parallelexploration.distributed;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.Set;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.stream.Collectors;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import jaicore.graphvisualizer.VisualizationWindow;
//import jaicore.graphvisualizer.TooltipGenerator;
//import jaicore.search.algorithms.interfaces.IORGraphSearch;
//import jaicore.search.algorithms.interfaces.IORGraphSearchFactory;
//import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
//import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.DistributedSearchCommunicationLayer;
//import jaicore.search.algorithms.standard.bestfirst.BestFirst;
//import jaicore.search.algorithms.standard.bestfirst.model.GraphGenerator;
//import jaicore.search.algorithms.standard.bestfirst.model.Node;
//import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
//import meka.core.A;
//
//public class DistributedOrSearchCoworker<T, A, L extends IGraphAlgorithmListener<V,E>, V extends Comparable<V>> implements IORGraphSearch<T, A, L, O> {
//
//	private final static Logger logger = LoggerFactory.getLogger(DistributedOrSearchCoworker.class);
//
//	private final IORGraphSearchFactory<T, A, V> algorithmFactory;
//	private final DistributedSearchCommunicationLayer<T, A, V> coworkerInterface;
//	protected final String id;
//	private final int searchTime;
//	private final int uptime;
//	private boolean shutdown = false;
//	private boolean showGraph;
//	private Class<?> tooltipGenerator;
//
//	public DistributedOrSearchCoworker(IORGraphSearchFactory<T, A, V> algorithmFactory, DistributedSearchCommunicationLayer<T, A, V> coworkerInterface, String id, int uptime, int searchTime,
//			boolean showGraph) {
//		super();
//		this.algorithmFactory = algorithmFactory;
//		this.coworkerInterface = coworkerInterface;
//		this.id = id;
//		this.searchTime = searchTime;
//		this.uptime = uptime;
//		this.showGraph = showGraph;
//		logger.info("Created new coworker {}", this.id);
//	}
//
//	@SuppressWarnings("unchecked")
//	public void cowork() {
//
//		final Thread runningThread = Thread.currentThread();
//		Timer timer = new Timer();
//		timer.schedule(new TimerTask() {
//
//			@Override
//			public void run() {
//				logger.info("Uptime of coworker {} ended; shutting it down. Note that the shutdown will be effective not until the current job is finished.", id);
//				shutdown = true;
//
//				/* if we have not been attached, we stop working */
//				if (!coworkerInterface.isAttached(id)) {
//					logger.info("Coworker has not been attached, so interrupting waiting thread.");
//					runningThread.interrupt();
//				}
//				coworkerInterface.unregister(id);
//			}
//		}, uptime);
//
//		/* register this coworker and setup the timer for unregister */
//		try {
//			coworkerInterface.register(this.id);
//			logger.info("Coworker has been attached.");
//		} catch (InterruptedException e) {
//			logger.info("Coworker was interrupted while waiting for attachment.");
//		}
//
//		/* now busily wait for jobs forever */
//		GraphGenerator<T, A> graphGenerator = null;
//		INodeEvaluator<T, V> nodeEvaluator = null;
//		try {
//			while (!shutdown || coworkerInterface.isAttached(this.id)) {
//
//				/* wait until a new job has arrived */
//				logger.info("Waiting for next job ...");
//				final Collection<Node<T, V>> nodes = coworkerInterface.nextJob(this.id);
//				logger.info("Found new job ...");
//				if (nodes == null || nodes.isEmpty()) {
//					logger.warn("Received NULL or EMPTY node list.");
//					break;
//				}
//
//				/* if this is the first job, get graph generator and node evaluator */
//				if (graphGenerator == null || nodeEvaluator == null) {
//					graphGenerator = coworkerInterface.getGraphGenerator();
//					nodeEvaluator = coworkerInterface.getNodeEvaluator();
//				}
//
//				/* if we became detached and want to shut down, leave loop */
//				if (shutdown && !coworkerInterface.isAttached(this.id)) {
//					logger.info("Coworker will be shutdown since shutdown signal has been received and the coworker is not attached.");
//					break;
//				}
//
//				/*
//				 * setup the search algorithm with the graph generator, and configure a time out
//				 */
//				IObservableORGraphSearch<T, A, V> searchAlgorithm = (IObservableORGraphSearch<T, A, V>) algorithmFactory.getSearch(graphGenerator, nodeEvaluator);
//				timer.schedule(new TimerTask() {
//					@Override
//					public void run() {
//						logger.info("Search timeout triggered. Shutting down search process.");
//						searchAlgorithm.cancel();
//					}
//				}, searchTime);
//
//				/* run the algorithm */
//				if (showGraph) {
//					VisualizationWindow<Node<T, V>> window = new VisualizationWindow<>(((IObservableORGraphSearch<T, A, V>) searchAlgorithm));
//					window.setTitle(this.id);
//					if (tooltipGenerator != null)
//						window.setTooltipGenerator((TooltipGenerator<Node<T, V>>) tooltipGenerator.getConstructor().newInstance());
//				}
//				List<T> solution;
//				List<Node<T, V>> solutionNodes = new ArrayList<>();
//				logger.info("Running coworker {} with: {}", this.id, nodes.stream().map(n -> n.getPoint()).collect(Collectors.toList()));
//				searchAlgorithm.bootstrap(nodes);
//				do {
//					solution = searchAlgorithm.nextSolution();
//					if (solution != null)
//						solutionNodes.add(searchAlgorithm.getInternalRepresentationOf(solution.get(solution.size() - 1)));
//				} while (solution != null);
//				logger.info("Coworker {} finished, reporting results and going to wait for new jobs.", this.id);
//
//				/* report results */
//				Collection<Node<T, V>> openNodes = searchAlgorithm.getOpenSnapshot();
//				logger.info("Reporting open list of size " + openNodes.size() + " and " + solutionNodes.size() + " solutions.");
//				DistributedComputationResult<T, V> result = new DistributedComputationResult<>(this.id, openNodes, solutionNodes);
//				coworkerInterface.reportResult(this.id, result);
//
//				/* if we returned nothing, detach for debugging */
//				if (openNodes.isEmpty() && solutionNodes.isEmpty()) {
//					logger.warn("Produced dead end!");
//				}
//			}
//		} catch (InterruptedException e) {
//			logger.info("Received interrupt.");
//		} catch (NoClassDefFoundError | ClassNotFoundException e) {
//			logger.error(
//					"Cannot perform the search as the class {} was not found on the classpath. This is probably a problem of serialization. Simply make sure that the class is on the classpath for the coworker.",
//					e.getMessage());
//		} catch (Throwable e) {
//			logger.error("Execution terminated unexpectedly. Please check error log!");
//			e.printStackTrace();
//		}
//
//		/* shutdown infrastructure */
//		timer.cancel();
//		if (coworkerInterface.isAttached(id))
//			coworkerInterface.detachCoworker(id);
//		else
//			coworkerInterface.unregister(id);
//		coworkerInterface.close();
//		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
//		logger.info("Terminating. Currently active threads: {}", threadSet);
//	}
//
//	public static <T, A, V extends Comparable<V>> void main(String[] args) {
//
//		if (args.length < 5) {
//			System.err.println("Need at least 5 args: communicationFolder, coworkerId, searchTime (s), upTime (s), showGraph[, numThreads]");
//			System.exit(1);
//		}
//
//		Path folder = Paths.get(args[0]);
//		String id = args[1];
//		int searchTime = Integer.parseInt(args[2]) * 1000;
//		int uptime = Integer.parseInt(args[3]) * 1000;
//		boolean showGraph = Boolean.parseBoolean(args[4]);
//		int threads = (args.length > 5) ? Integer.parseInt(args[5]) : 1;
//
//		logger.info("Using {} threads.", threads);
//		String strClassPath = System.getProperty("java.class.path");
//		logger.info("Classpath is " + strClassPath);
//
//		IORGraphSearchFactory<T, A, V> factory = (gen, eval) -> {
//			BestFirst<T, A, V> search = new jaicore.search.algorithms.standard.bestfirst.BestFirst<>(gen, eval);
//			if (threads > 1)
//				search.parallelizeNodeExpansion(threads);
//			search.setTimeoutForComputationOfF(1000, n -> null);
//			return search;
//		};
//		DistributedSearchCommunicationLayer<T, A, V> communicationLayer = new FolderBasedDistributedSearchCommunicationLayer<>(folder, false);
//		DistributedOrSearchCoworker<T, A, V> coworker = new DistributedOrSearchCoworker<>(factory, communicationLayer, id, uptime, searchTime, showGraph);
//		if (args.length > 5) {
//			try {
//				coworker.tooltipGenerator = Class.forName(args[6]);
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
//		coworker.cowork();
//		System.exit(0);
//	}
//}
