package autofe.algorithm.hasco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.evaluation.AbstractHASCOFEEvaluator;
import autofe.algorithm.hasco.evaluation.AbstractHASCOFENodeEvaluator;
import autofe.algorithm.hasco.evaluation.AbstractHASCOFEObjectEvaluator;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.algorithm.hasco.filter.meta.FilterPipelineFactory;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import autofe.util.EvaluationUtils;
import autofe.util.FilterUtils;
import hasco.core.HASCOFD;
import hasco.core.Solution;
import hasco.model.Component;
import hasco.serialization.ComponentLoader;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.ml.WekaUtil;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.search.structure.core.Node;
import weka.core.Instance;
import weka.core.Instances;

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

	private final int[] inputShape;

	// Logging
	private static Logger logger = LoggerFactory.getLogger(HASCOFE.class);
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
			AbstractHASCOFEObjectEvaluator benchmark, final int[] inputShape) {

		if (config == null || !config.exists())
			throw new IllegalArgumentException(
					"The file " + config + " is null or does not exist and cannot be used by ML-Plan");

		this.inputShape = inputShape;
		this.configFile = config;
		this.initializeHASCOSearch(data, nodeEvaluator, benchmark);
	}

	private void initializeHASCOSearch(final DataSet data, AbstractHASCOFENodeEvaluator nodeEvaluator,
			AbstractHASCOFEObjectEvaluator benchmark) { // AbstractHASCOFEObjectEvaluator

		// benchmark
		IObjectEvaluator<FilterPipeline, Double> objectEvaluator = null;
		if (benchmark != null) {
			benchmark.setData(data);
			objectEvaluator = benchmark;
		} else {
			objectEvaluator = (n) -> {
				// Empty pipe
				if (n.getFilters() == null)
					return AbstractHASCOFEEvaluator.MAX_EVAL_VALUE;
				else
					return new Random(new Random().nextInt(1000)).nextDouble();
			};
		}

		if (nodeEvaluator != null) {
			nodeEvaluator.setHascoFE(this);
			nodeEvaluator.setData(data);

			this.hasco = new HASCOFD<>(new FilterPipelineFactory(this.inputShape), nodeEvaluator, "FilterPipeline",
					objectEvaluator);
		} else {
			this.hasco = new HASCOFD<>(new FilterPipelineFactory(this.inputShape), n -> null, "FilterPipeline",
					objectEvaluator);
		}
		this.hasco.setNumberOfCPUs(Runtime.getRuntime().availableProcessors());

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

	public static List<Instances> generateRandomDataSets(final int dataset, final double usedDataSetSize,
			final int maxSolutionCount, final int maxPipelineSize, final int timeout) throws Exception {
		/* load image dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(dataset);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(new Random().nextInt() * 1000),
				usedDataSetSize);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		for (Instance inst : split.get(0)) {
			intermediate.add(DataSetUtils.instanceToMatrixByDataSet(inst, dataset));
		}
		logger.info("Finished intermediate calculations.");
		DataSet originDataSet = new DataSet(split.get(0), intermediate);

		HASCOFE hascoFE = new HASCOFE(new File("model/catalano/catalano.json"),
				EvaluationUtils.getRandomNodeEvaluator(maxPipelineSize), new DataSet(split.get(0), intermediate), null,
				DataSetUtils.getInputShapeByDataSet(dataset));
		hascoFE.setLoggerName("autofe");
		hascoFE.runSearch(timeout);

		// Calculate solution data sets
		List<Instances> result = new ArrayList<>();
		result.add(originDataSet.getInstances());

		logger.debug("Found solutions: " + hascoFE.getFoundClassifiers().toString());
		List<HASCOFESolution> solutions = new ArrayList<>(hascoFE.getFoundClassifiers());
		logger.debug("Found " + solutions.size() + " solutions.");
		Collections.shuffle(solutions);

		Iterator<HASCOFESolution> solIt = solutions.iterator();

		int solCounter = 0;
		while (solIt.hasNext() && solCounter < maxSolutionCount) {
			HASCOFESolution nextSol = solIt.next();

			FilterPipeline pipe = nextSol.getSolution();

			// Skip empty pipes
			if (pipe.getFilters() == null)
				continue;

			logger.debug("Applying solution pipe " + pipe.toString());

			result.add(pipe.applyFilter(originDataSet, true).getInstances());
			solCounter++;
		}

		logger.debug("Got all randomly generated data sets.");

		return result;
	}
}
