package de.upb.crc901.mlplan.search.algorithms;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.ClassifierSolutionAnnotation;
import de.upb.crc901.mlplan.core.MySQLMLPlanExperimentLogger;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.graphvisualizer.TooltipGenerator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.structure.core.Node;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;

public abstract class GraphBasedPipelineSearcher<T, A, V extends Comparable<V>> extends AbstractClassifier implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(GraphBasedPipelineSearcher.class);

	/* configuration */
	private long timeOfStart;
	private int numberOfCPUs = 1;
	private boolean showGraph;
	private int timeout;
	private Random random;
	private transient MySQLMLPlanExperimentLogger experimentLogger;
	private File solutionLogFile;

	protected final Map<Classifier, ClassifierSolutionAnnotation<V>> solutionAnnotationCache = new HashMap<>();
	protected transient Queue<Classifier> solutions;
	private Classifier selectedModel = null;

	protected transient TooltipGenerator<Node<T, V>> tooltipGenerator;

	public GraphBasedPipelineSearcher() {
	}

	public GraphBasedPipelineSearcher(Random random, int timeout, boolean showGraph) {
		super();
		this.showGraph = showGraph;
		this.timeout = timeout;
		this.random = random;
	}

	public Random getRandom() {
		return random;
	}

	protected abstract IObservableORGraphSearch<T, A, V> getSearch(Instances data) throws Exception;

	protected abstract Classifier convertPathToPipeline(List<T> path);

	protected void beforeSearch() {
	}

	protected boolean shouldSearchTerminate(long timeRemaining) {
		return timeRemaining < 1000;
	}

	protected void logSolution(Classifier pl, V score) {
		if (score instanceof Number && experimentLogger != null)
			experimentLogger.addEvaluationEntry(this, pl, (double) score);
	}

	public void findPipelines(Instances data) throws Exception {

		/* get the search algorithm */
		IObservableORGraphSearch<T, A, V> search = getSearch(data);
		initSolutions();

		/* set timeout for the search */
		final long start = System.currentTimeMillis();
		AtomicBoolean timedout = new AtomicBoolean(false);
		Thread timeoutConroller = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					long remaining;
					while (true) {

						/* compute remaining time until timeout */
						remaining = timeout - (System.currentTimeMillis() - start);
						if (shouldSearchTerminate(remaining)) {
							logger.info("Initializing graph search shutdown.");
							timedout.set(true);
							search.cancel();
							logger.info("Sent cancel signal to the search algorithm...");
							return;
						}
						Thread.sleep(1000);
					}
				} catch (Throwable e) {
					System.err.println("Timer received the following exception (canceling search due to this):");
					e.printStackTrace();
					search.cancel();
				}
			}
		});
		timeoutConroller.setName("GraphBased Pipeline Optimizer Timer");
		timeoutConroller.start();

		/* identify goal pipelines and train the best of them */
		if (showGraph) {
			SimpleGraphVisualizationWindow<Node<T, V>> w = new SimpleGraphVisualizationWindow<>(search.getEventBus());
			if (tooltipGenerator != null)
				w.getPanel().setTooltipGenerator(tooltipGenerator);
		}
		List<T> solution = null;
		while (true) {
			if ((solution = search.nextSolution()) == null) {
				logger.info("Search algorithm has signaled that no more solutions exist.");
				search.cancel();
				break;
			}
			if (timedout.get()) {
				logger.info("Timeout was triggered.");
				break;
			}
			if (search.getFOfReturnedSolution(solution) == null) {
				logger.error("Received solution without f-value: {}", solution);
				continue;
			}

			/* derive solution pipeline from plan */
			Classifier mlp = modifyPipeline(convertPathToPipeline(solution));
			if (mlp == null)
				throw new IllegalArgumentException("Pipeline obtained from " + solution + " is NULL!");
			V solutionQuality = search.getFOfReturnedSolution(solution);
			logSolution(mlp, solutionQuality);
			Integer fTime = (Integer) search.getAnnotationOfReturnedSolution(solution, "fTime");
			if (fTime == null) {
				logger.warn("No time information available for {}", mlp);
				fTime = 0;
			}
			ClassifierSolutionAnnotation<V> annotation = new ClassifierSolutionAnnotation<>(solutionQuality, fTime);
			solutionAnnotationCache.put(mlp, annotation);
			solutions.add(mlp);

			/* log solution stats */
			if (solutionLogFile != null) {
				if (!solutionLogFile.getParentFile().exists())
					solutionLogFile.getParentFile().mkdirs();
				// if (annotation instanceof MLPipelineSolutionAnnotation) {
				// try (FileWriter fw = new FileWriter(solutionLogFile, true)) {
				// fw.write(castedAnnotation.getTimeUntilSolutionWasFound() + ", " + castedAnnotation.f() + ", " + castedAnnotation.getGenerationNumberOfGoalNode() + ", "
				// + castedAnnotation.getTimeForFComputation() + ", " + mlp.getCreationPlan() + "\n");
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				// }
			}
		}
		search.cancel();
	}

	protected Classifier modifyPipeline(Classifier mlp) {
		return mlp;
	}

	protected void logSolution(Classifier mlp) {
		ClassifierSolutionAnnotation<V> annotation = solutionAnnotationCache.get(mlp);
		logger.info("Registered solution #{} with f-value {} (computation took {}). Solution: {}", solutions.size(), annotation.getF(), annotation.getFTime(), mlp);
	};

	protected void afterSearch() {
	}

	public ClassifierSolutionAnnotation<V> getAnnotation(Classifier pipeline) {
		return solutionAnnotationCache.get(pipeline);
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {

		timeOfStart = System.currentTimeMillis();

		/* search for solutions */
		this.findPipelines(data);

		logger.info("Finished search.");

		/* choose a model */
		if (solutions.isEmpty()) {
			logger.warn("No model identified. Cannot build the classifier.");
			return;
		}
		selectedModel = selectModel();
		if (selectedModel == null)
			throw new IllegalStateException("No model chosen even though there were solutions!");

		/* train the selected model with all data */
		logger.info("Now training the selected classifier on the whole data.");
		selectedModel.buildClassifier(data);
		logger.info("Classifier built.");
	}

	protected abstract Classifier selectModel();

	@Override
	public double classifyInstance(Instance instance) throws Exception {
		if (selectedModel == null)
			throw new IllegalStateException("Cannot make predictions since the model has not been learned yet.");
		return selectedModel.classifyInstance(instance);
	}
	
	public double[] classifyInstances(Instances instances) throws Exception {
		if (selectedModel == null)
			throw new IllegalStateException("Cannot make predictions since the model has not been learned yet.");
		Method m = MethodUtils.getMatchingAccessibleMethod(selectedModel.getClass(), "classifyInstances", Instances.class);
		if (m == null)
			throw new IllegalStateException("Selected model " + selectedModel.getClass() + " has no support for querying multiple instances at a time.");
		return (double[]) m.invoke(selectedModel, instances);
	}

	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {
		if (selectedModel == null)
			throw new IllegalStateException("Cannot make predictions since the model has not been learned yet.");
		return selectedModel.distributionForInstance(instance);
	}

	@Override
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.disableAll();

		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.NUMERIC_ATTRIBUTES);
		result.enable(Capability.DATE_ATTRIBUTES);
		result.enable(Capability.STRING_ATTRIBUTES);
		result.enable(Capability.RELATIONAL_ATTRIBUTES);
		result.enable(Capability.MISSING_VALUES);

		// class
		result.enable(Capability.NOMINAL_CLASS);
		result.enable(Capability.NUMERIC_CLASS);
		result.enable(Capability.DATE_CLASS);
		result.enable(Capability.MISSING_CLASS_VALUES);

		// instances
		result.setMinimumNumberInstances(1);
		return result;
	}

	public Classifier getSelectedModel() {
		return selectedModel;
	}

	public void setTooltipGenerator(TooltipGenerator<Node<T, V>> tooltipGenerator) {
		this.tooltipGenerator = tooltipGenerator;
		this.showGraph = true;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	public int getNumberOfCPUs() {
		return numberOfCPUs;
	}

	public void setNumberOfCPUs(int numberOfCPUs) {
		this.numberOfCPUs = numberOfCPUs;
	}

	public File getSolutionLogFile() {
		return solutionLogFile;
	}

	public void setSolutionLogFile(File solutionLogFile) {
		this.solutionLogFile = solutionLogFile;
	}

	public void cancel() {
		logger.warn("Received cancel signal, but ignoring it.");
	}

	public MySQLMLPlanExperimentLogger getExperimentLogger() {
		return experimentLogger;
	}

	public void setExperimentLogger(MySQLMLPlanExperimentLogger experimentLogger) {
		this.experimentLogger = experimentLogger;
	}

	public long getTimeOfStart() {
		return timeOfStart;
	}

	protected void initSolutions() {
		solutions = new PriorityQueue<>(new Comparator<Classifier>() {

			@Override
			public int compare(Classifier o1, Classifier o2) {
				return solutionAnnotationCache.get(o1).getF().compareTo(solutionAnnotationCache.get(o2).getF());
			}
		});
	}

	public boolean isShowGraph() {
		return showGraph;
	}

	public void setShowGraph(boolean showGraph) {
		this.showGraph = showGraph;
	}
}
