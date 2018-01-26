package de.upb.crc901.mlplan.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jaicore.ml.WekaUtil;
import jaicore.planning.model.ceoc.CEOCAction;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

/**
 * 
 * @author Felix Mohr
 *
 */
@SuppressWarnings("serial")
public class MLPipeline implements Classifier, Serializable {

	private final List<CEOCAction> creationPlan;
	private final List<SuvervisedFilterSelector> preprocessors = new ArrayList<>();
	private final Classifier baseClassifier;
	private boolean trained = false;
	private long timeForTrainingPreprocessors, timeForTrainingClassifier, timeForExecutingPreprocessor, timeForExecutingClassifier;

	public MLPipeline(List<CEOCAction> creationPlan, List<SuvervisedFilterSelector> preprocessor, Classifier baseClassifier) {
		super();
		this.creationPlan = creationPlan;
		if (baseClassifier == null)
			throw new IllegalArgumentException("Base classifier must not be null!");
		this.preprocessors.addAll(preprocessors);
		this.baseClassifier = baseClassifier;
	}

	public MLPipeline(List<CEOCAction> creationPlan, ASSearch searcher, ASEvaluation evaluator, Classifier baseClassifier) {
		super();
		this.creationPlan = creationPlan;
		if (baseClassifier == null)
			throw new IllegalArgumentException("Base classifier must not be null!");
		if (searcher != null && evaluator != null) {
			AttributeSelection selector = new AttributeSelection();
			selector.setSearch(searcher);
			selector.setEvaluator(evaluator);
			preprocessors.add(new SuvervisedFilterSelector(searcher, evaluator, selector));
		}
		this.baseClassifier = baseClassifier;
	}

	public List<CEOCAction> getCreationPlan() {
		return creationPlan;
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {

		/* reduce dimensionality */
		long start = System.currentTimeMillis();
		for (SuvervisedFilterSelector pp : preprocessors) {

			/* if the filter has not been trained yet, do so now and store it */
			if (!pp.isPrepared()) {
				try {
					start = System.currentTimeMillis();
					pp.prepare(data);
					timeForTrainingPreprocessors = System.currentTimeMillis() - start;
					int newNumberOfClasses = pp.apply(data).numClasses();
					if (data.numClasses() != newNumberOfClasses) {
						System.out.println(pp.getSelector() + " changed number of classes from " + data.numClasses() + " to " + newNumberOfClasses);
					}
				} catch (NullPointerException e) {
					System.err.println("Problems with training pipeline: ");
					System.err.println(MLUtil.getJavaCodeFromPlan(creationPlan));
					e.printStackTrace();
				}
			}

			/* now apply the attribute selector */
			data = pp.apply(data);
		}

		/* build classifier based on reduced data */
		start = System.currentTimeMillis();
		baseClassifier.buildClassifier(data);
		timeForTrainingClassifier = System.currentTimeMillis() - start;
		trained = true;
	}

	private Instance applyPreprocessors(Instance data) throws Exception {
		long start = System.currentTimeMillis();
		for (SuvervisedFilterSelector pp : preprocessors) {
			data = pp.apply(data);
			timeForExecutingPreprocessor = System.currentTimeMillis() - start;
		}
		return data;
	}

	@Override
	public double classifyInstance(Instance arg0) throws Exception {
		if (!trained)
			throw new IllegalStateException("Cannot make predictions on untrained pipeline!");
		arg0 = applyPreprocessors(arg0);
		long start = System.currentTimeMillis();
		double result = baseClassifier.classifyInstance(arg0);
		timeForExecutingClassifier = System.currentTimeMillis() - start;
		return result;
	}

	@Override
	public double[] distributionForInstance(Instance arg0) throws Exception {
		if (!trained)
			throw new IllegalStateException("Cannot make predictions on untrained pipeline!");
		if (arg0 == null)
			throw new IllegalArgumentException("Cannot make predictions for null-instance");
		arg0 = applyPreprocessors(arg0);
		if (arg0 == null)
			throw new IllegalStateException("The filter has turned the instance into NULL");
		long start = System.currentTimeMillis();
		double[] result = baseClassifier.distributionForInstance(arg0);
		timeForExecutingClassifier = System.currentTimeMillis() - start;
		return result;
	}

	@Override
	public Capabilities getCapabilities() {
		return baseClassifier.getCapabilities();
	}

	public Classifier getBaseClassifier() {
		return baseClassifier;
	}

	public List<SuvervisedFilterSelector> getPreprocessors() {
		return preprocessors;
	}

	@Override
	public String toString() {
		return getPreprocessors() + " (preprocessors), " + WekaUtil.getClassifierDescriptor(getBaseClassifier()) + " (classifier)";
	}

	public long getTimeForTrainingPreprocessor() {
		return timeForTrainingPreprocessors;
	}

	public long getTimeForTrainingClassifier() {
		return timeForTrainingClassifier;
	}

	public long getTimeForExecutingPreprocessor() {
		return timeForExecutingPreprocessor;
	}

	public long getTimeForExecutingClassifier() {
		return timeForExecutingClassifier;
	}
}
