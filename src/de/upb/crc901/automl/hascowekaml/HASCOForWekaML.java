package de.upb.crc901.automl.hascowekaml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.core.HASCO.HASCOSolutionIterator;
import hasco.core.HASCOFD;
import hasco.core.Solution;
import hasco.model.Component;
import hasco.model.NumericParameter;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.MulticlassEvaluator;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class HASCOForWekaML implements IObservableGraphAlgorithm<TFDNode, String> {
	
	private static final Logger logger = LoggerFactory.getLogger(HASCOForWekaML.class);
	
	public static class HASCOForWekaMLSolution {

		private Solution<ForwardDecompositionSolution,Classifier> hascoSolution;

		public HASCOForWekaMLSolution(Solution<ForwardDecompositionSolution, Classifier> hascoSolution) {
			super();
			this.hascoSolution = hascoSolution;
		}

		public Classifier getClassifier() {
			return hascoSolution.getSolution();
		}
	}
	
	private boolean isCanceled = false;
	private Collection<Object> listeners = new ArrayList<>();
	private HASCOSolutionIterator hascoRun;
	private INodeEvaluator<TFDNode,Double> preferredNodeEvaluator = n -> null;
	
	private Queue<HASCOForWekaMLSolution> solutionsFoundByHASCO = new PriorityQueue<>(new Comparator<HASCOForWekaMLSolution>() {

			@Override
			public int compare(HASCOForWekaMLSolution o1, HASCOForWekaMLSolution o2) {
				return (int)Math.round(10000 * (getQualityHASCODeterminedForSolution(o2) - getQualityHASCODeterminedForSolution(o1)));
			}
	});
	
	public void gatherSolutions(Instances data, int timeoutInMS) {
		
		if (isCanceled) {
			throw new IllegalStateException("HASCO has already been canceled. Cannot gather results anymore.");
		}
		
		long start = System.currentTimeMillis();
		long deadline = start + timeoutInMS;
		
		/* create algorithm */
		Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramConfigs = new HashMap<>();
		HASCOFD<Classifier> hasco = new HASCOFD<Classifier>(groundComponent -> {
			Component component = groundComponent.getComponent();
			Map<String, String> paramValues = groundComponent.getParameterValues();
			String className = component.getName();
			try {
				List<String> params = new ArrayList<>();
				for (Parameter p : component.getParameters()) {
					if (paramValues.containsKey(p.getName())) {
							params.add("-" + p.getName());
							params.add(paramValues.get(p.getName()));
					}
				}
				String[] paramsAsArray = params.toArray(new String[] {});
				return AbstractClassifier.forName(className, paramsAsArray);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}, preferredNodeEvaluator, paramConfigs, "classifier", new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(new Random(3)), 3, data, .7f));
		
		/* configuring existing components */
		Component c = null;
		Parameter p;
		Map<Parameter, ParameterRefinementConfiguration> paramConfig;
		c = new Component("weka.classifiers.trees.RandomTree");
		c.addProvidedInterface("classifier");
		c.addProvidedInterface("baseclassifier");
		p = new NumericParameter("M", true, 1, 1, 10);
		paramConfig = new HashMap<>();
		paramConfig.put(p, new ParameterRefinementConfiguration(8, 1));
		c.addParameter(p);
		p = new NumericParameter("K", true, 0, 0, 10);
		paramConfig.put(p, new ParameterRefinementConfiguration(2, 1));
		c.addParameter(p);
		paramConfigs.put(c, paramConfig);
		hasco.addComponent(c);
		
		/* add all listeners to HASCO */
		listeners.forEach(l -> hasco.registerListener(l));
		
		/* run HASCO */
		hascoRun = hasco.iterator();
		boolean deadlineReached = false;
		while (!isCanceled && hascoRun.hasNext() && (timeoutInMS <= 0 || !(deadlineReached = System.currentTimeMillis() >= deadline))) {
			HASCOForWekaMLSolution nextSolution = new HASCOForWekaMLSolution(hascoRun.next());
			solutionsFoundByHASCO.add(nextSolution);
		}
		if (deadlineReached)
			logger.info("Deadline has been reached");
		else if (isCanceled) {
			logger.info("Interrupting HASCO due to cancel.");
		}
	}
	
	public void cancel() {
		isCanceled = true;
		if (this.hascoRun != null)
			this.hascoRun.cancel();
	}

	public Queue<HASCOForWekaMLSolution> getFoundClassifiers() {
		return new LinkedList<>(solutionsFoundByHASCO);
	}
	
	public Map<String,Object> getAnnotationsOfSolution(HASCOForWekaMLSolution solution) {
		return hascoRun.getAnnotationsOfSolution(solution.hascoSolution);
	}
	
	public int getTimeHASCONeededToEvaluateSolution(HASCOForWekaMLSolution solution) {
		return (int)getAnnotationsOfSolution(solution).get("fTime");
	}
	
	public double getQualityHASCODeterminedForSolution(HASCOForWekaMLSolution solution) {
		return (double)getAnnotationsOfSolution(solution).get("f");
	}
	
	public HASCOForWekaMLSolution getCurrentlyBestSolution() {
		return solutionsFoundByHASCO.peek();
	}

	@Override
	public void registerListener(Object listener) {
		listeners.add(listener);
	}

	public INodeEvaluator<TFDNode, Double> getPreferredNodeEvaluator() {
		return preferredNodeEvaluator;
	}

	public void setPreferredNodeEvaluator(INodeEvaluator<TFDNode, Double> preferredNodeEvaluator) {
		this.preferredNodeEvaluator = preferredNodeEvaluator;
	}
}
