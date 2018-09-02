package de.upb.crc901.automl.hascowekaml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

import hasco.core.HASCOFD;
import hasco.core.Solution;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import hasco.query.Factory;
import hasco.serialization.ComponentLoader;
import jaicore.basic.IObjectEvaluator;
import jaicore.graph.IObservableGraphAlgorithm;
import jaicore.ml.multilabel.evaluators.F1AverageMultilabelEvaluator;
import jaicore.ml.multilabel.evaluators.MonteCarloCrossValidationEvaluator;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import meka.classifiers.multilabel.MultiLabelClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.OptionHandler;

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
			return (MultiLabelClassifier) produceClassifier(groundComponent);
		}

		private Classifier produceClassifier(ComponentInstance groundComponent) {

			/* collect basic information about the component */
			Component component = groundComponent.getComponent();
			Map<String, String> paramValues = groundComponent.getParameterValues();
			String className = component.getName();

			/* now try to create an object of this component */
			try {
				List<String> params = new ArrayList<>();
				for (Parameter p : component.getParameters()) {
					if (paramValues.containsKey(p.getName())) {

						/* ignore activator params, which are only used to control the search */
						if (p.getName().contains("Activator"))
							continue;

						/* if this is a boolean flag and the value is false, omit it */
						String value = paramValues.get(p.getName());
						if (value.equals("false"))
							continue;

						if (className.contains("meka"))
							params.add(p.getName().replaceAll("_", "-"));
						else if (className.contains("weka"))
							params.add("-" + p.getName());

						/* if this is a boolean flag and the value is positive, just add the name */
						if (!value.equals("true"))
							params.add(value);
					}
				}
				String[] paramsAsArray = params.toArray(new String[] {});
				Classifier c = (Classifier) Class.forName(className).newInstance();
				if (c instanceof OptionHandler) {
					try {
						((OptionHandler) c).setOptions(paramsAsArray);
					} catch (Exception e) {
						logger.error("Invalid option array for classifier {}: {}. Exception: {}. Error message: {}", className, params, e.getClass().getName(), e.getMessage());
					}
				}
				
				/* if this is an enhanced classifier, set its base classifier */
				if (component.getRequiredInterfaces().size() > 1)
					throw new IllegalArgumentException("This factory can currently only handle at most one required interface per component");
				if (component.getRequiredInterfaces().size() == 1) {
					if ((c instanceof SingleClassifierEnhancer)) {
					SingleClassifierEnhancer cc = (SingleClassifierEnhancer) c;
					ComponentInstance baseClassifierCI = groundComponent.getSatisfactionOfRequiredInterfaces().values().iterator().next(); // there is only one required interface
					if (baseClassifierCI == null)
						throw new IllegalStateException(
								"The required interface \"Classifier\" of component " + groundComponent.getComponent().getName() + " has not been satisifed!");
					Classifier baseClassifier = produceClassifier(baseClassifierCI);
					cc.setClassifier(baseClassifier);
					}
					else if(c instanceof SMO) {
						ComponentInstance kernel = groundComponent.getSatisfactionOfRequiredInterfaces().values().iterator().next(); // there is only one required interface
						System.out.println("Kernel " + kernel);
					}
					else
						throw new IllegalArgumentException(
							"Required interfaces are currently only supported for SingleClassifierInhancer or SMO objects (and the base classifier must be their required interface). The presented class "
									+ c.getClass().getName() + " does not satisfy this requirement.");
				}
				return c;

			} catch (ClassNotFoundException | NoClassDefFoundError e) {
				logger.error("Could not find a class with class name {}", className);
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

	}

	private boolean isCanceled = false;
	private int numberOfCPUs = 1;
	private Collection<Object> listeners = new ArrayList<>();
	private HASCOFD<MultiLabelClassifier,Double>.HASCOSolutionIterator hascoRun;
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
		IObjectEvaluator<MultiLabelClassifier, Double> mccv = new MonteCarloCrossValidationEvaluator(new F1AverageMultilabelEvaluator(new Random(0)), 1, data, 0.7f);
		HASCOFD<MultiLabelClassifier,Double> hasco = new HASCOFD<MultiLabelClassifier,Double>(cl.getComponents(), cl.getParamConfigs(), new MEKAFactory(), "MLClassifier", mccv);
		hasco.setPreferredNodeEvaluator(preferredNodeEvaluator);
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
