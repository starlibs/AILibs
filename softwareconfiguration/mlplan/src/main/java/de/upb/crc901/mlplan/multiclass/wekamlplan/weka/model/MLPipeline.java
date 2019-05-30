package de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.WekaUtil;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author Felix Mohr
 *
 */
@SuppressWarnings("serial")
public class MLPipeline extends SingleClassifierEnhancer implements Classifier, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(MLPipeline.class);

	private final List<SupervisedFilterSelector> preprocessors = new ArrayList<>();

	private boolean trained = false;

	private int timeForTrainingPreprocessors;
	private int timeForTrainingClassifier;

	private DescriptiveStatistics timeForExecutingPreprocessors;
	private DescriptiveStatistics timeForExecutingClassifier;

	public MLPipeline(final List<SupervisedFilterSelector> preprocessors, final Classifier baseClassifier) {
		super();
		if (baseClassifier == null) {
			throw new IllegalArgumentException("Base classifier must not be null!");
		}
		this.preprocessors.addAll(preprocessors);
		super.setClassifier(baseClassifier);
	}

	public MLPipeline(final ASSearch searcher, final ASEvaluation evaluator, final Classifier baseClassifier) {
		super();
		if (baseClassifier == null) {
			throw new IllegalArgumentException("Base classifier must not be null!");
		}
		if (searcher != null && evaluator != null) {
			AttributeSelection selector = new AttributeSelection();
			selector.setSearch(searcher);
			selector.setEvaluator(evaluator);
			this.preprocessors.add(new SupervisedFilterSelector(searcher, evaluator, selector));
		}
		super.setClassifier(baseClassifier);
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {

		/* reduce dimensionality */
		long start;
		int numAttributesBefore = data.numAttributes();
		logger.info("Starting to build the preprocessors of the pipeline.");

		for (SupervisedFilterSelector pp : this.preprocessors) {

			/* if the filter has not been trained yet, do so now and store it */
			if (!pp.isPrepared()) {
				try {
					start = System.currentTimeMillis();
					pp.prepare(data);
					this.timeForTrainingPreprocessors = (int) (System.currentTimeMillis() - start);
					int newNumberOfClasses = pp.apply(data).numClasses();
					if (data.numClasses() != newNumberOfClasses) {
						logger.info("{} changed number of classes from {} to {}", pp.getSelector(), data.numClasses(), newNumberOfClasses);
					}
				} catch (NullPointerException e) {
					logger.error("Could not apply preprocessor", e);
				}
			}

			/* now apply the attribute selector */
			data = pp.apply(data);
		}
		logger.info("Reduced number of attributes from {} to {}", numAttributesBefore, data.numAttributes());

		/* build classifier based on reduced data */
		start = System.currentTimeMillis();
		super.getClassifier().buildClassifier(data);
		this.timeForTrainingClassifier = (int) (System.currentTimeMillis() - start);
		this.trained = true;
		this.timeForExecutingPreprocessors = new DescriptiveStatistics();
		this.timeForExecutingClassifier = new DescriptiveStatistics();
	}

	private Instance applyPreprocessors(Instance data) throws Exception {
		long start = System.currentTimeMillis();
		for (SupervisedFilterSelector pp : this.preprocessors) {
			data = pp.apply(data);
		}
		this.timeForExecutingPreprocessors.addValue((int) (System.currentTimeMillis() - start));
		return data;
	}

	@Override
	public double classifyInstance(Instance arg0) throws Exception {
		if (!this.trained) {
			throw new IllegalStateException("Cannot make predictions on untrained pipeline!");
		}
		int numAttributesBefore = arg0.numAttributes();
		arg0 = this.applyPreprocessors(arg0);
		if (numAttributesBefore != arg0.numAttributes()) {
			logger.info("Reduced number of attributes from {} to {}", numAttributesBefore, arg0.numAttributes());
		}
		long start = System.currentTimeMillis();
		double result = super.getClassifier().classifyInstance(arg0);
		this.timeForExecutingClassifier.addValue((System.currentTimeMillis() - start));
		return result;
	}

	public double[] classifyInstances(final Instances arg0) throws Exception {
		int n = arg0.size();
		double[] answers = new double[n];
		for (int i = 0; i < n; i++) {
			answers[i] = this.classifyInstance(arg0.get(i));
		}
		return answers;
	}

	@Override
	public double[] distributionForInstance(Instance arg0) throws Exception {
		if (!this.trained) {
			throw new IllegalStateException("Cannot make predictions on untrained pipeline!");
		}
		if (arg0 == null) {
			throw new IllegalArgumentException("Cannot make predictions for null-instance");
		}
		arg0 = this.applyPreprocessors(arg0);
		if (arg0 == null) {
			throw new IllegalStateException("The filter has turned the instance into NULL");
		}
		long start = System.currentTimeMillis();
		double[] result = super.getClassifier().distributionForInstance(arg0);
		this.timeForExecutingClassifier.addValue((int) (System.currentTimeMillis() - start));
		return result;
	}

	@Override
	public Capabilities getCapabilities() {
		return super.getClassifier().getCapabilities();
	}

	public Classifier getBaseClassifier() {
		return super.getClassifier();
	}

	public List<SupervisedFilterSelector> getPreprocessors() {
		return this.preprocessors;
	}

	@Override
	public String toString() {
		return this.getPreprocessors() + " (preprocessors), " + WekaUtil.getClassifierDescriptor(this.getBaseClassifier()) + " (classifier)";
	}

	public long getTimeForTrainingPreprocessor() {
		return this.timeForTrainingPreprocessors;
	}

	public long getTimeForTrainingClassifier() {
		return this.timeForTrainingClassifier;
	}

	public DescriptiveStatistics getTimeForExecutingPreprocessor() {
		return this.timeForExecutingPreprocessors;
	}

	public DescriptiveStatistics getTimeForExecutingClassifier() {
		return this.timeForExecutingClassifier;
	}
}
