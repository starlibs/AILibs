package de.upb.crc901.mlplan.multiclass.wekamlplan.sophisticated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.upb.crc901.mlplan.multiclass.wekamlplan.sophisticated.featuregen.FeatureGenerator;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * 
 * @author Felix Mohr
 *
 */
@SuppressWarnings("serial")
public class MLSophisticatedPipeline implements Classifier, FeatureGenerator, Serializable {

	private final List<FeatureGenerator> featureGenerators = new ArrayList<>();
	private final List<FeaturePreprocessor> featurePreprocessors = new ArrayList<>();
	private final List<FeaturePreprocessor> featureSelectors = new ArrayList<>();
	private final Classifier classifier;
	private boolean trained = false;
	private long timeForTrainingPreprocessors, timeForTrainingClassifier, timeForExecutingPreprocessor, timeForExecutingClassifier;
	private Instances emptyReferenceDataset;

	public MLSophisticatedPipeline(List<FeatureGenerator> featureGenerators, List<FeaturePreprocessor> preprocessors, List<FeaturePreprocessor> featureSelectors,
			Classifier baseClassifier) {
		super();
		if (baseClassifier == null)
			throw new IllegalArgumentException("Base classifier must not be null!");
		this.featureGenerators.addAll(featureGenerators);
		this.featurePreprocessors.addAll(preprocessors);
		this.featureSelectors.addAll(featureSelectors);
		this.classifier = baseClassifier;
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {

		/* determine features to be created */
		long start = System.currentTimeMillis();
		Instances mergedInstances = new Instances(data);
		int f = data.numAttributes();

		/* generate features */
		for (FeatureGenerator pp : featureGenerators) {

			/* if the filter has not been trained yet, do so now and store it */
			if (!pp.isPrepared()) {
				try {
					start = System.currentTimeMillis();
					pp.prepare(data);
					timeForTrainingPreprocessors = System.currentTimeMillis() - start;
					// int newNumberOfClasses = pp.apply(data).numClasses();
					// if (data.numClasses() != newNumberOfClasses) {
					// System.out.println(pp.getSelector() + " changed number of classes from " + data.numClasses() + " to " + newNumberOfClasses);
					// }
				} catch (NullPointerException e) {
					System.err.println("Problems with training pipeline");
					e.printStackTrace();
				}
			}
			Instances modifiedInstances = pp.apply(data);
			if (modifiedInstances == null)
				throw new IllegalStateException("Feature Generator " + pp + " has generated a null-dataset!");

			/* now apply the attribute selector */
			for (int i = 0; i < modifiedInstances.numAttributes(); i++)
				modifiedInstances.renameAttribute(modifiedInstances.attribute(i), "f" + (f++));
			mergedInstances = Instances.mergeInstances(mergedInstances, modifiedInstances);
			mergedInstances.setClassIndex(data.classIndex());
		}
		data = mergedInstances;

		/* preprocess features */
		for (FeaturePreprocessor pp : featurePreprocessors) {
			pp.prepare(data);
			data = pp.apply(data);
			if (data.classIndex() < 0)
				throw new IllegalStateException("Preprocessor " + pp + " has removed class index!");
		}

		/* feature selection */
		int fCount = data.numAttributes();
		for (FeaturePreprocessor pp : featureSelectors) {
			pp.prepare(data);
			data = pp.apply(data);
			if (data.classIndex() < 0)
				throw new IllegalStateException("Preprocessor " + pp + " has removed class index!");
		}
		System.out.println("Reduced features from " + fCount + " to " + data.numAttributes());

		/* build classifier based on reduced data */
		emptyReferenceDataset = new Instances(data);
		emptyReferenceDataset.clear();
		start = System.currentTimeMillis();
		classifier.buildClassifier(data);
		timeForTrainingClassifier = System.currentTimeMillis() - start;
		trained = true;
	}

	private Instance applyPreprocessors(Instance data) throws Exception {
		long start = System.currentTimeMillis();

		/* create features */
		Instance mergedInstance = new DenseInstance(data);
		mergedInstance.setDataset(data.dataset());
		for (FeatureGenerator pp : featureGenerators) {

			Instances mergedDatasetA = new Instances(mergedInstance.dataset());
			mergedDatasetA.clear();
			mergedDatasetA.add(mergedInstance);
			Instance modifiedInstance = pp.apply(data);
			if (modifiedInstance.dataset() == null)
				throw new IllegalStateException("Instance was detached from dataset by " + pp);

			Instances mergedDatasetB = modifiedInstance.dataset();
			Instances mergedDataset = Instances.mergeInstances(mergedDatasetA, mergedDatasetB);
			mergedDataset.setClassIndex(mergedDatasetA.classIndex());
			mergedInstance = mergedInstance.mergeInstance(modifiedInstance);
			mergedInstance.setDataset(mergedDataset);
			timeForExecutingPreprocessor = System.currentTimeMillis() - start;
		}
		data = mergedInstance;

		/* preprocess features */
		for (FeaturePreprocessor pp : featurePreprocessors) {
			data = pp.apply(data);
		}

		/* feature selection */
		for (FeaturePreprocessor pp : featureSelectors) {
			data = pp.apply(data);
		}
		return data;
	}

	@Override
	public double classifyInstance(Instance arg0) throws Exception {
		if (!trained)
			throw new IllegalStateException("Cannot make predictions on untrained pipeline!");
		arg0 = applyPreprocessors(arg0);
		long start = System.currentTimeMillis();
		double result = classifier.classifyInstance(arg0);
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
		double[] result = classifier.distributionForInstance(arg0);
		timeForExecutingClassifier = System.currentTimeMillis() - start;
		return result;
	}

	@Override
	public Capabilities getCapabilities() {
		return classifier.getCapabilities();
	}

	public Classifier getBaseClassifier() {
		return classifier;
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

	@Override
	public void prepare(Instances data) throws Exception {
		buildClassifier(data);
	}

	private Instances getEmptyProbingResultDataset() {
		if (!isPrepared())
			throw new IllegalStateException("Cannot determine empty dataset, because the pipeline has not been trained yet.");
		ArrayList<Attribute> atts = new ArrayList<>();
		List<String> attributeValues = WekaUtil.getClassesDeclaredInDataset(emptyReferenceDataset);
		for (String att : attributeValues) {
			atts.add(new Attribute("probe_classprob_" + att + "_" + this));
		}
		Instances empty = new Instances("probing", atts, 0);
		return empty;
	}

	@Override
	public Instance apply(Instance data) throws Exception {
		double[] classProbs = distributionForInstance(data);
		Instance newInst = new DenseInstance(classProbs.length);
		Instances dataset = getEmptyProbingResultDataset();
		dataset.add(newInst);
		newInst.setDataset(dataset);
		for (int i = 0; i < classProbs.length; i++)
			newInst.setValue(i, classProbs[i]);
		return newInst;
	}

	@Override
	public Instances apply(Instances data) throws Exception {
		Instances probingResults = new Instances(getEmptyProbingResultDataset());
		for (Instance inst : data) {
			Instance probedInst = apply(inst);
			probedInst.setDataset(probingResults);
			probingResults.add(probedInst);
		}
		return probingResults;
	}

	@Override
	public boolean isPrepared() {
		return this.trained;
	}
}
