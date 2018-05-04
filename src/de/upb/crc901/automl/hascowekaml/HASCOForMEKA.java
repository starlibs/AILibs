package de.upb.crc901.automl.hascowekaml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
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
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import hasco.query.Factory;
import hasco.serialization.ComponentLoader;
import jaicore.basic.IObjectEvaluator;
import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.ml.multilabel.evaluators.F1AverageMultilabelEvaluator;
import jaicore.ml.multilabel.evaluators.MonteCarloCrossValidationEvaluator;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.MLUtils;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.core.Instances;

public class HASCOForMEKA implements IObservableGraphAlgorithm<TFDNode, String> {

	private static final Logger logger = LoggerFactory.getLogger(HASCOForMEKA.class);

	public static class HASCOForMEKASolution {

		private Solution<ForwardDecompositionSolution, MultiLabelClassifier, Double> hascoSolution;

		public HASCOForMEKASolution(Solution<ForwardDecompositionSolution, MultiLabelClassifier, Double> hascoSolution) {
			super();
			this.hascoSolution = hascoSolution;
		}

		public MultiLabelClassifier getClassifier() {
			return hascoSolution.getSolution();
		}

		public double getScore() {
			return hascoSolution.getScore();
		}

		public int getTimeForScoreComputation() {
			return hascoSolution.getTimeToComputeScore();
		}
	}

	private static class MEKAFactory implements Factory<MultiLabelClassifier> {

		@Override
		public MultiLabelClassifier getComponentInstantiation(ComponentInstance groundComponent) {
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
				
				/* create main classification algorithm and set its base learner */
				MultiLabelClassifier c = (MultiLabelClassifier) Class.forName(className).newInstance();
				c.setOptions(paramsAsArray);
				if (c instanceof SingleClassifierEnhancer) {
					SingleClassifierEnhancer cc = (SingleClassifierEnhancer)c;
					ComponentInstance baseClassifierCI = groundComponent.getSatisfactionOfRequiredInterfaces().get("Classifier");
					Classifier baseClassifier = AbstractClassifier.forName(baseClassifierCI.getComponent().getName(), new String[] {});
					cc.setClassifier(baseClassifier);
				}
				System.err.println(Arrays.toString(c.getOptions()));
				return c;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

	}

	private boolean isCanceled = false;
	private int numberOfCPUs = 1;
	private Collection<Object> listeners = new ArrayList<>();
	private HASCOSolutionIterator hascoRun;
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator = n -> null;

	private Queue<HASCOForMEKASolution> solutionsFoundByHASCO = new PriorityQueue<>(new Comparator<HASCOForMEKASolution>() {

		@Override
		public int compare(HASCOForMEKASolution o1, HASCOForMEKASolution o2) {
			return (int) Math.round(10000 * (o1.getScore() - o2.getScore()));
		}
	});

	public void gatherSolutions(Instances data, int timeoutInMS) {

		if (isCanceled) {
			throw new IllegalStateException("HASCO has already been canceled. Cannot gather results anymore.");
		}

		long start = System.currentTimeMillis();
		long deadline = start + timeoutInMS;
		
		/* derive existing components */
		ComponentLoader cl = new ComponentLoader();
		try {
			cl.loadComponents(new File("testrsc/acml2018/mlplan-multilabel.json"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		/* create algorithm */
		IObjectEvaluator<MultiLabelClassifier, Double> mccv = new MonteCarloCrossValidationEvaluator(new F1AverageMultilabelEvaluator(new Random(0)), 5, data, 0.7f);
		HASCOFD<MultiLabelClassifier> hasco = new HASCOFD<MultiLabelClassifier>(new MEKAFactory(), preferredNodeEvaluator, cl.getParamConfigs(), "MLClassifier", mccv);
		hasco.addComponents(cl.getComponents());
		hasco.setNumberOfCPUs(numberOfCPUs);

		
		/* add all listeners to HASCO */
		listeners.forEach(l -> hasco.registerListener(l));
		listeners.forEach(l -> hasco.registerListenerForSolutionEvaluations(l));

		/* run HASCO */
		hascoRun = hasco.iterator();
		boolean deadlineReached = false;
		while (!isCanceled && hascoRun.hasNext() && (timeoutInMS <= 0 || !(deadlineReached = System.currentTimeMillis() >= deadline))) {
			HASCOForMEKASolution nextSolution = new HASCOForMEKASolution(hascoRun.next());
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

	public Queue<HASCOForMEKASolution> getFoundClassifiers() {
		return new LinkedList<>(solutionsFoundByHASCO);
	}

	public HASCOForMEKASolution getCurrentlyBestSolution() {
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

	public int getNumberOfCPUs() {
		return numberOfCPUs;
	}

	public void setNumberOfCPUs(int numberOfCPUs) {
		this.numberOfCPUs = numberOfCPUs;
	}
}
