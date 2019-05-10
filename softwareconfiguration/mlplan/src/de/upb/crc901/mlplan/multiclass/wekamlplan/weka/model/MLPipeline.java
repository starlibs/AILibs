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
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;


/**
 * 
 * @author Felix Mohr
 *
 */
@SuppressWarnings("serial")
public class MLPipeline implements Classifier, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(MLPipeline.class);
	
	private static final String preprocessorsString = " (preprocessors), ";
	private static final String preprocessorsMatchingString = " [(]preprocessors[)], ";
	private static final String classifierString = " (classifier)";
	private static final String classifierMatchingString = " [(]classifier[)]";
	
	private final List<SupervisedFilterSelector> preprocessors = new ArrayList<>();
	private final Classifier baseClassifier;
	private boolean trained = false;
	private int timeForTrainingPreprocessors, timeForTrainingClassifier;
	private DescriptiveStatistics timeForExecutingPreprocessors, timeForExecutingClassifier;

	public MLPipeline(List<SupervisedFilterSelector> preprocessors, Classifier baseClassifier) {
		super();
		if (baseClassifier == null)
			throw new IllegalArgumentException("Base classifier must not be null!");
		this.preprocessors.addAll(preprocessors);
		this.baseClassifier = baseClassifier;
	}

	public MLPipeline(ASSearch searcher, ASEvaluation evaluator, Classifier baseClassifier) {
		super();
		if (baseClassifier == null)
			throw new IllegalArgumentException("Base classifier must not be null!");
		if (searcher != null && evaluator != null) {
			AttributeSelection selector = new AttributeSelection();
			selector.setSearch(searcher);
			selector.setEvaluator(evaluator);
			preprocessors.add(new SupervisedFilterSelector(searcher, evaluator, selector));
		}
		this.baseClassifier = baseClassifier;
	}
	
	public MLPipeline(String pipelineRepresentation) throws Exception {
		// Get parts
		String[] parts = pipelineRepresentation.split(preprocessorsMatchingString);

		// If there is a preprocessor
		if (parts.length > 1) {
			// Get preprocessor (can currently only get 1)
			parts[0] = parts[0].substring(1, parts[0].length() - 1);
			if (!parts[0].equals("")) {
				preprocessors.add(new SupervisedFilterSelector(parts[0]));
			}
		}

		// Get base classifier
		parts[1] = parts[1].replaceAll(classifierMatchingString, "");
		baseClassifier = WekaUtil.fromClassifierDescriptor(parts[1]);
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {
		
		/* reduce dimensionality */
		long start = System.currentTimeMillis();
		int numAttributesBefore = data.numAttributes();
		logger.info("Starting to build the preprocessors of the pipeline.");
		
		for (SupervisedFilterSelector pp : preprocessors) {

			/* if the filter has not been trained yet, do so now and store it */
			if (!pp.isPrepared()) {
				try {
					start = System.currentTimeMillis();
					pp.prepare(data);
					timeForTrainingPreprocessors = (int)(System.currentTimeMillis() - start);
					int newNumberOfClasses = pp.apply(data).numClasses();
					if (data.numClasses() != newNumberOfClasses) {
						System.out.println(pp.getSelector() + " changed number of classes from " + data.numClasses() + " to " + newNumberOfClasses);
					}
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}

			/* now apply the attribute selector */
			data = pp.apply(data);
		}
		logger.info("Reduced number of attributes from {} to {}", numAttributesBefore, data.numAttributes());

		/* build classifier based on reduced data */
		start = System.currentTimeMillis();
		baseClassifier.buildClassifier(data);
		timeForTrainingClassifier = (int)(System.currentTimeMillis() - start);
		trained = true;
		timeForExecutingPreprocessors = new DescriptiveStatistics();
		timeForExecutingClassifier = new DescriptiveStatistics();
	}

	private Instance applyPreprocessors(Instance data) throws Exception {
		long start = System.currentTimeMillis();
		for (SupervisedFilterSelector pp : preprocessors) {
			data = pp.apply(data);
		}
		timeForExecutingPreprocessors.addValue((int)(System.currentTimeMillis() - start));
		return data;
	}

	@Override
	public double classifyInstance(Instance arg0) throws Exception {
		if (!trained)
			throw new IllegalStateException("Cannot make predictions on untrained pipeline!");
		int numAttributesBefore = arg0.numAttributes();
		arg0 = applyPreprocessors(arg0);
		if (numAttributesBefore != arg0.numAttributes())
			logger.info("Reduced number of attributes from {} to {}", numAttributesBefore, arg0.numAttributes());
		long start = System.currentTimeMillis();
		double result = baseClassifier.classifyInstance(arg0);
		timeForExecutingClassifier.addValue((System.currentTimeMillis() - start));
		return result;
	}
	
	public double[] classifyInstances(Instances arg0) throws Exception {
		int n = arg0.size();
		double[] answers = new double[n];
		for (int i = 0; i < n; i++)
			answers[i] = classifyInstance(arg0.get(i));
		return answers;
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
		timeForExecutingClassifier.addValue((int)(System.currentTimeMillis() - start));
		return result;
	}

	@Override
	public Capabilities getCapabilities() {
		return baseClassifier.getCapabilities();
	}

	public Classifier getBaseClassifier() {
		return baseClassifier;
	}

	public List<SupervisedFilterSelector> getPreprocessors() {
		return preprocessors;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getPreprocessors());
		builder.append(preprocessorsString);
		builder.append(WekaUtil.getClassifierDescriptor(getBaseClassifier()));
		builder.append(classifierString);
		return builder.toString();
	}

	public long getTimeForTrainingPreprocessor() {
		return timeForTrainingPreprocessors;
	}

	public long getTimeForTrainingClassifier() {
		return timeForTrainingClassifier;
	}

	public DescriptiveStatistics getTimeForExecutingPreprocessor() {
		return timeForExecutingPreprocessors;
	}

	public DescriptiveStatistics getTimeForExecutingClassifier() {
		return timeForExecutingClassifier;
	}

	public MLPipeline clone() {
		List<SupervisedFilterSelector> clonedPreprocessing = new ArrayList<>();
		try {
			
			/* clone preprocessing */
			for (SupervisedFilterSelector s : preprocessors) {
				ASSearch search = s.getSearcher();
				ASSearch searchClone = ASSearch.forName(search.getClass().getName(), (search instanceof OptionHandler) ? ((OptionHandler) search).getOptions() : new String[] {});
				ASEvaluation eval = s.getEvaluator();
				ASEvaluation evalClone = ASEvaluation.forName(eval.getClass().getName(), (eval instanceof OptionHandler) ? ((OptionHandler) eval).getOptions() : new String[] {});
				clonedPreprocessing.add(new SupervisedFilterSelector(searchClone, evalClone));
			}
			
			/* clone classifier */
			Classifier classifierClone = WekaUtil.cloneClassifier(baseClassifier);
			return new MLPipeline(clonedPreprocessing, classifierClone);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
