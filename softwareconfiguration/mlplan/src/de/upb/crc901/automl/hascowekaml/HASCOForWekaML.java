
package de.upb.crc901.automl.hascowekaml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.core.HASCOFD;
import hasco.core.Solution;
import hasco.serialization.ComponentLoader;
import jaicore.basic.ILoggingCustomizable;
import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.MulticlassEvaluator;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class HASCOForWekaML implements IObservableGraphAlgorithm<TFDNode, String>, ILoggingCustomizable {

	/* logging */
	private Logger logger = LoggerFactory.getLogger(HASCOForWekaML.class);
	private String loggerName;

	public static class HASCOForWekaMLSolution extends Solution<ForwardDecompositionSolution, Classifier, Double> {

		public HASCOForWekaMLSolution(Solution<ForwardDecompositionSolution, Classifier, Double> solution) {
			super(solution);
		}
	}

	private boolean isCanceled = false;
	private Collection<Object> listeners = new ArrayList<>();
	private HASCOFD<Classifier>.HASCOSolutionIterator hascoRun;
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator = n -> null;
	private final File wekaSpaceConfigurationFile; // this is a hasco file describing the 
	
	public HASCOForWekaML(File hascoConfigurationFile) {
		this.wekaSpaceConfigurationFile = hascoConfigurationFile;
	}

	private Queue<HASCOForWekaMLSolution> solutionsFoundByHASCO = new PriorityQueue<>(new Comparator<HASCOForWekaMLSolution>() {

		@Override
		public int compare(final HASCOForWekaMLSolution o1, final HASCOForWekaMLSolution o2) {
			return o1.getScore().compareTo(o2.getScore());
		}
	});

	public void gatherSolutions(final Instances data, final int timeoutInMS) throws IOException {

		if (this.isCanceled) {
			throw new IllegalStateException("HASCO has already been canceled. Cannot gather results anymore.");
		}

		long start = System.currentTimeMillis();
		long deadline = start + timeoutInMS;

		/* create algorithm */
		HASCOFD<Classifier> hasco = new HASCOFD<>(new WEKAPipelineFactory(), this.preferredNodeEvaluator, "AbstractClassifier", new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(new Random(3)), 3, data, .7f));
		if (this.loggerName != null && this.loggerName.length() > 0)
			hasco.setLoggerName(loggerName + ".hasco");

		/* configuring existing components */
		ComponentLoader cl = new ComponentLoader();
		cl.loadComponents(wekaSpaceConfigurationFile);
		hasco.addComponents(cl.getComponents());
		hasco.addParamRefinementConfigurations(cl.getParamConfigs());
		

		/* add all listeners to HASCO */
		this.listeners.forEach(l -> hasco.registerListener(l));

		/* run HASCO */
		this.hascoRun = hasco.iterator();
		boolean deadlineReached = false;
		while (!this.isCanceled && this.hascoRun.hasNext() && (timeoutInMS <= 0 || !(deadlineReached = System.currentTimeMillis() >= deadline))) {
			HASCOForWekaMLSolution nextSolution = new HASCOForWekaMLSolution(this.hascoRun.next());
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

	public Queue<HASCOForWekaMLSolution> getFoundClassifiers() {
		return new LinkedList<>(this.solutionsFoundByHASCO);
	}
	
	public HASCOForWekaMLSolution getCurrentlyBestSolution() {
		return this.solutionsFoundByHASCO.peek();
	}

	@Override
	public void registerListener(final Object listener) {
		this.listeners.add(listener);
	}

	public INodeEvaluator<TFDNode, Double> getPreferredNodeEvaluator() {
		return this.preferredNodeEvaluator;
	}

	public void setPreferredNodeEvaluator(final INodeEvaluator<TFDNode, Double> preferredNodeEvaluator) {
		this.preferredNodeEvaluator = preferredNodeEvaluator;
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
}
