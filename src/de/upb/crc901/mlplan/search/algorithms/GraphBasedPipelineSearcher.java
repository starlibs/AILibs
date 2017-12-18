package de.upb.crc901.mlplan.search.algorithms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.MLPipeline;
import de.upb.crc901.mlplan.core.MLPipelineSolutionAnnotation;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.graphvisualizer.TooltipGenerator;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.structure.core.Node;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public abstract class GraphBasedPipelineSearcher<T, A, V extends Comparable<V>> implements Classifier {
	private static final Logger logger = LoggerFactory.getLogger(GraphBasedPipelineSearcher.class);

	/* configuration */
	private int numberOfCPUs = 1;
	private boolean showGraph;
	private int timeout;
	private Random random;
	private File solutionLogFile;
	protected final Map<MLPipeline, MLPipelineSolutionAnnotation<T, V>> solutionAnnotationCache = new HashMap<>();
	protected final Queue<MLPipeline> solutions = new PriorityQueue<>(new Comparator<MLPipeline>() {

		@Override
		public int compare(MLPipeline o1, MLPipeline o2) {
			return solutionAnnotationCache.get(o1).f().compareTo(solutionAnnotationCache.get(o2).f());
		}
	});
	private MLPipeline selectedModel = null;
	protected int numberOfConsideredSolutions;
	protected int selectionDepth;
	protected TooltipGenerator<Node<T, V>> tooltipGenerator;
	protected IObservableORGraphSearch<T, A, V> search; 

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

	protected abstract MLPipeline convertPathToPipeline(List<T> path);

	protected void beforeSearch() {
	}

	protected boolean shouldSearchTerminate(long timeRemaining) {
		return timeRemaining < 1000;
	}

	public void findPipelines(Instances data) throws Exception {

		/* get the search algorithm */
		search = getSearch(data);

		/* set timeout for the search */
		final long start = System.currentTimeMillis();
		AtomicBoolean timedout = new AtomicBoolean(false);
		Thread timeoutConroller = new Thread(new Runnable() {

			@Override
			public void run() {

				while (true) {

					/* compute remaining time until timeout */
					long remaining = timeout - (System.currentTimeMillis() - start);
					if (shouldSearchTerminate(remaining)) {
						logger.info("Initializing graph search shutdown.");
						timedout.set(true);
						search.cancel();
						logger.info("Sent cancel signal to the search algorithm...");
						return;
					}

					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
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
		while (((solution = search.nextSolution()) != null) && !timedout.get()) {
			if (search.getFOfReturnedSolution(solution) == null) {
				logger.error("Received solution without f-value: {}", solution);
				continue;
			}

			/* derive solution pipeline from plan */
			MLPipeline mlp = modifyPipeline(convertPathToPipeline(solution));
			MLPipelineSolutionAnnotation<T, V> annotation = (MLPipelineSolutionAnnotation<T, V>) search.getAnnotationOfReturnedSolution(solution);
			solutionAnnotationCache.put(mlp, annotation);
			solutions.add(mlp);

			/* log solution stats */
			if (solutionLogFile != null) {
				if (!solutionLogFile.getParentFile().exists())
					solutionLogFile.getParentFile().mkdirs();
				try (FileWriter fw = new FileWriter(solutionLogFile, true)) {
					fw.write(annotation.getTimeUntilSolutionWasFound() + ", " + annotation.f() + ", " + annotation.getGenerationNumberOfGoalNode() + ",  "
							+ annotation.getTimeForFComputation() + ", " + mlp.getCreationPlan() + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (!timedout.get()) {
			logger.info(
					"Search was not interrupted, but there will be no more solutions. So we shutdown the search now (could be relevant if it needs to shutdown a thread pool etc.)");
			search.cancel();
		}
	}

	protected MLPipeline modifyPipeline(MLPipeline mlp) { return mlp; }
	
	protected void logSolution(MLPipeline mlp) {
		logger.info("Registered solution #{} with f-value {} (computation took {}). Solution: {}", solutions.size(), solutionAnnotationCache.get(mlp).f(),
				solutionAnnotationCache.get(mlp).getTimeForFComputation(), mlp);
	};

	protected void afterSearch() {
	}

	public MLPipelineSolutionAnnotation<T, V> getAnnotation(MLPipeline pipeline) {
		return solutionAnnotationCache.get(pipeline);
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {

		/* search for solutions */
		this.findPipelines(data);
		logger.info("Finished search.");

		/* choose a model */
		if (solutions.isEmpty())
			throw new IllegalStateException("No model identified. Cannot build the classifier.");
		selectedModel = selectModel();
		if (selectedModel == null)
			throw new IllegalStateException("No model chosen even though there were solutions!");
		
		/* train the selected model with all data */
		logger.info("Now training the selected classifier on the whole data.");
		selectedModel.buildClassifier(data);
		logger.info("Classifier built.");
	}

	protected abstract MLPipeline selectModel();

	@Override
	public double classifyInstance(Instance instance) throws Exception {
		if (selectedModel == null)
			throw new IllegalStateException("Cannot make predictions since the model has not been learned yet.");
		return selectedModel.classifyInstance(instance);
	}

	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {
		if (selectedModel == null)
			throw new IllegalStateException("Cannot make predictions since the model has not been learned yet.");
		return selectedModel.distributionForInstance(instance);
	}

	@Override
	public Capabilities getCapabilities() {
		if (selectedModel == null)
			throw new IllegalStateException("Cannot make assertions about capabilities since the model has not been defined yet.");
		return selectedModel.getCapabilities();
	}

	public MLPipeline getSelectedModel() {
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

	public int getNumberOfConsideredSolutions() {
		return numberOfConsideredSolutions;
	}

	public void setNumberOfConsideredSolutions(int numberOfConsideredSolutions) {
		this.numberOfConsideredSolutions = numberOfConsideredSolutions;
	}

	public int getSelectionDepth() {
		return selectionDepth;
	}

	public void setSelectionDepth(int selectionDepth) {
		this.selectionDepth = selectionDepth;
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
		logger.info("Received cancel signal.");
		search.cancel();
	}
}
