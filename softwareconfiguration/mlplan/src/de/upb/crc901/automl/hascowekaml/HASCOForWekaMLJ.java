
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

import de.upb.crc901.automl.PreferenceBasedNodeEvaluator;
import hasco.core.HASCOFDWithParameterPruning;
import hasco.core.HASCOProblemReduction;
import hasco.core.Solution;
import hasco.model.Component;
import hasco.serialization.ComponentLoader;
import jaicore.basic.FileUtil;
import jaicore.basic.ILoggingCustomizable;
import jaicore.graph.IObservableGraphAlgorithm;
import jaicore.logging.LoggerUtil;
import jaicore.ml.evaluation.ClassifierEvaluator;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.MulticlassEvaluator;
import jaicore.ml.evaluation.TimeoutableEvaluator;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.interfaces.ISolutionEvaluator;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.uncertainty.OversearchAvoidanceConfig;
import jaicore.search.structure.core.GraphGenerator;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class HASCOForWekaMLJ implements IObservableGraphAlgorithm<TFDNode, String>, ILoggingCustomizable {

	/* logging */
	private Logger logger = LoggerFactory.getLogger(HASCOForWekaMLJ.class);
	private String loggerName;

	public static class HASCOForWekaMLSolutionJ extends Solution<ForwardDecompositionSolution, Classifier, Double> {

		public HASCOForWekaMLSolutionJ(final Solution<ForwardDecompositionSolution, Classifier, Double> solution) {
			super(solution);
		}
	}

	private boolean isCanceled = false;
	private OversearchAvoidanceConfig<TFDNode> oversearchAvoidanceConfig = new OversearchAvoidanceConfig<>(
			OversearchAvoidanceConfig.OversearchAvoidanceMode.NONE);
	private Collection<Object> listeners = new ArrayList<>();
	private HASCOFDWithParameterPruning<Classifier, Double>.HASCOSolutionIterator hascoRun;
	private HASCOFDWithParameterPruning<Classifier, Double> hasco;
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator = null;
	private final File wekaSpaceConfigurationFile; // this is a hasco file describing the
	private int timeoutForSingleFEvaluation = -1;
	private double importanceThreshold;
	private int minNumSamples;
	private boolean useParameterImportanceEstimation;

	public HASCOForWekaMLJ(final File hascoConfigurationFile, double importanceThreshold, int minNumSamples, boolean userParameterImportanceEstimation) {
		this.wekaSpaceConfigurationFile = hascoConfigurationFile;
		this.importanceThreshold = importanceThreshold;
		this.minNumSamples = minNumSamples;
		this.useParameterImportanceEstimation = userParameterImportanceEstimation;
	}

	private Queue<HASCOForWekaMLSolutionJ> solutionsFoundByHASCO = new PriorityQueue<>(
			new Comparator<HASCOForWekaMLSolutionJ>() {

				@Override
				public int compare(final HASCOForWekaMLSolutionJ o1, final HASCOForWekaMLSolutionJ o2) {
					return o1.getScore().compareTo(o2.getScore());
				}
			});

	public void gatherSolutions(final Instances data, final int timeoutInMS) throws IOException {

		if (this.isCanceled) {
			throw new IllegalStateException("HASCO has already been canceled. Cannot gather results anymore.");
		}
		logger.info("Starting to gather solutions for {}ms.", timeoutInMS);

		long start = System.currentTimeMillis();
		long deadline = start + timeoutInMS;

		/* configuring existing components */
		logger.debug("Loading components ...");
		ComponentLoader cl = new ComponentLoader();
		cl.loadComponents(this.wekaSpaceConfigurationFile);

		/* create algorithm */
		if (this.preferredNodeEvaluator == null) {
			try {
				this.preferredNodeEvaluator = new PreferenceBasedNodeEvaluator(cl.getComponents(),
						FileUtil.readFileAsList("model/combined/preferredNodeEvaluator.txt"));
			} catch (IOException e) {
				this.logger.error("Problem loading the preference-based node evaluator. Details:\n{}",
						LoggerUtil.getExceptionInfo(e));
				return;
			}
		}

		ClassifierEvaluator ce;
		if (this.timeoutForSingleFEvaluation > 0) {
			ce = new TimeoutableEvaluator(
					new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(new Random(3)), 3, data, .7f),
					this.timeoutForSingleFEvaluation * 1000);
		} else {
			ce = new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(new Random(3)), 3, data, .7f);
		}

		/* create algorithm */
//		hasco = new HASCOFDWithParameterPruning<>(cl.getComponents(), cl.getParamConfigs(), new WEKAPipelineFactory(), "AbstractClassifier", ce, this.oversearchAvoidanceConfig);
		hasco = new HASCOFDWithParameterPruning<>(cl.getComponents(), cl.getParamConfigs(), new WEKAPipelineFactory(), "AbstractClassifier", ce, this.oversearchAvoidanceConfig, importanceThreshold, minNumSamples, useParameterImportanceEstimation);
		hasco.setPreferredNodeEvaluator(this.preferredNodeEvaluator);
		
		if (this.loggerName != null && this.loggerName.length() > 0) {
			hasco.setLoggerName(this.loggerName + ".hasco");
		}


		/* add all listeners to HASCO */
		logger.info("Registering listeners ...");
		this.listeners.forEach(l -> hasco.registerListener(l));

		/* run HASCO */
		this.hascoRun = hasco.iterator();
		boolean deadlineReached = false;
		logger.info("Entering loop ...");
		while (!this.isCanceled && this.hascoRun.hasNext()
				&& (timeoutInMS <= 0 || !(deadlineReached = System.currentTimeMillis() >= deadline))) {
			HASCOForWekaMLSolutionJ nextSolution = new HASCOForWekaMLSolutionJ(this.hascoRun.next());
			this.solutionsFoundByHASCO.add(nextSolution);
		}
		if (deadlineReached) {
			this.logger.info("Deadline has been reached");
		} else if (this.isCanceled) {
			this.logger.info("Interrupting HASCO due to cancel.");
		}
		else
			this.logger.info("HASCO finished.");
	}

	public void cancel() {
		this.isCanceled = true;
		if (this.hascoRun != null) {
			this.hascoRun.cancel();
		}
	}

	public Queue<HASCOForWekaMLSolutionJ> getFoundClassifiers() {
		return new LinkedList<>(this.solutionsFoundByHASCO);
	}

	public HASCOForWekaMLSolutionJ getCurrentlyBestSolution() {
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

	public void setTimeoutForSingleFEvaluation(final int timeoutForSingleFEvaluation) {
		System.out.println("timeoutForSingleFEvaluation=" + timeoutForSingleFEvaluation);
		this.timeoutForSingleFEvaluation = timeoutForSingleFEvaluation;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	public void setOversearchAvoidanceMode(final OversearchAvoidanceConfig oversearchAvoidanceConfig) {
		this.oversearchAvoidanceConfig = oversearchAvoidanceConfig;
	}

	public Collection<Component> getComponents() {
		if (this.hasco == null || this.hasco.getComponents() == null || this.hasco.getComponents().size() == 0) {
			return null;
		} else {
			return this.hasco.getComponents();
		}
	}
	
	public ISolutionEvaluator<TFDNode, Double> getSolutionEvaluator() {
		return hasco.getSolutionEvaluator();
	}
}
