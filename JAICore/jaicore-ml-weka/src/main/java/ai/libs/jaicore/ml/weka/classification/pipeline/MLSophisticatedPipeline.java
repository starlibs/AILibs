package ai.libs.jaicore.ml.weka.classification.pipeline;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.classification.pipeline.featuregen.FeatureGenerator;
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
	private long timeForTrainingPreprocessors;
	private long timeForTrainingClassifier;
	private long timeForExecutingPreprocessor;
	private long timeForExecutingClassifier;
	private Instances emptyReferenceDataset;

	public MLSophisticatedPipeline(final List<FeatureGenerator> featureGenerators, final List<FeaturePreprocessor> preprocessors, final List<FeaturePreprocessor> featureSelectors, final Classifier baseClassifier) {
		super();
		if (baseClassifier == null) {
			throw new IllegalArgumentException("Base classifier must not be null!");
		}
		this.featureGenerators.addAll(featureGenerators);
		this.featurePreprocessors.addAll(preprocessors);
		this.featureSelectors.addAll(featureSelectors);
		this.classifier = baseClassifier;
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {

		/* determine features to be created */
		long start;
		Instances mergedInstances = new Instances(data);
		int f = data.numAttributes();

		/* generate features */
		for (FeatureGenerator pp : this.featureGenerators) {

			/* if the filter has not been trained yet, do so now and store it */
			if (!pp.isPrepared()) {
				start = System.currentTimeMillis();
				pp.prepare(data);
				this.timeForTrainingPreprocessors = System.currentTimeMillis() - start;
			}
			Instances modifiedInstances = pp.apply(data);
			if (modifiedInstances == null) {
				throw new IllegalStateException("Feature Generator " + pp + " has generated a null-dataset!");
			}

			/* now apply the attribute selector */
			for (int i = 0; i < modifiedInstances.numAttributes(); i++) {
				modifiedInstances.renameAttribute(modifiedInstances.attribute(i), "f" + (f++));
			}
			mergedInstances = Instances.mergeInstances(mergedInstances, modifiedInstances);
			mergedInstances.setClassIndex(data.classIndex());
		}
		data = mergedInstances;

		/* preprocess features */
		for (FeaturePreprocessor pp : this.featurePreprocessors) {
			pp.prepare(data);
			data = pp.apply(data);
			if (data.classIndex() < 0) {
				throw new IllegalStateException("Preprocessor " + pp + " has removed class index!");
			}
		}

		/* feature selection */
		for (FeaturePreprocessor pp : this.featureSelectors) {
			pp.prepare(data);
			data = pp.apply(data);
			if (data.classIndex() < 0) {
				throw new IllegalStateException("Preprocessor " + pp + " has removed class index!");
			}
		}

		/* build classifier based on reduced data */
		this.emptyReferenceDataset = new Instances(data);
		this.emptyReferenceDataset.clear();
		start = System.currentTimeMillis();
		this.classifier.buildClassifier(data);
		this.timeForTrainingClassifier = System.currentTimeMillis() - start;
		this.trained = true;
	}

	private Instance applyPreprocessors(Instance data) throws PreprocessingException {
		long start = System.currentTimeMillis();

		/* create features */
		Instance mergedInstance = new DenseInstance(data);
		mergedInstance.setDataset(data.dataset());
		for (FeatureGenerator pp : this.featureGenerators) {

			Instances mergedDatasetA = new Instances(mergedInstance.dataset());
			mergedDatasetA.clear();
			mergedDatasetA.add(mergedInstance);
			Instance modifiedInstance = pp.apply(data);
			if (modifiedInstance.dataset() == null) {
				throw new IllegalStateException("Instance was detached from dataset by " + pp);
			}

			Instances mergedDatasetB = modifiedInstance.dataset();
			Instances mergedDataset = Instances.mergeInstances(mergedDatasetA, mergedDatasetB);
			mergedDataset.setClassIndex(mergedDatasetA.classIndex());
			mergedInstance = mergedInstance.mergeInstance(modifiedInstance);
			mergedInstance.setDataset(mergedDataset);
			this.timeForExecutingPreprocessor = System.currentTimeMillis() - start;
		}
		data = mergedInstance;

		/* preprocess features */
		for (FeaturePreprocessor pp : this.featurePreprocessors) {
			data = pp.apply(data);
		}

		/* feature selection */
		for (FeaturePreprocessor pp : this.featureSelectors) {
			data = pp.apply(data);
		}
		return data;
	}

	@Override
	public double classifyInstance(Instance arg0) throws Exception {
		if (!this.trained) {
			throw new IllegalStateException("Cannot make predictions on untrained pipeline!");
		}
		arg0 = this.applyPreprocessors(arg0);
		long start = System.currentTimeMillis();
		double result = this.classifier.classifyInstance(arg0);
		this.timeForExecutingClassifier = System.currentTimeMillis() - start;
		return result;
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
		double[] result = this.classifier.distributionForInstance(arg0);
		this.timeForExecutingClassifier = System.currentTimeMillis() - start;
		return result;
	}

	@Override
	public Capabilities getCapabilities() {
		return this.classifier.getCapabilities();
	}

	public Classifier getBaseClassifier() {
		return this.classifier;
	}

	public long getTimeForTrainingPreprocessor() {
		return this.timeForTrainingPreprocessors;
	}

	public long getTimeForTrainingClassifier() {
		return this.timeForTrainingClassifier;
	}

	public long getTimeForExecutingPreprocessor() {
		return this.timeForExecutingPreprocessor;
	}

	public long getTimeForExecutingClassifier() {
		return this.timeForExecutingClassifier;
	}

	@Override
	public void prepare(final Instances data) throws PreprocessingException {
		try {
			this.buildClassifier(data);
		} catch (Exception e) {
			throw new PreprocessingException(e);
		}
	}

	private Instances getEmptyProbingResultDataset() {
		if (!this.isPrepared()) {
			throw new IllegalStateException("Cannot determine empty dataset, because the pipeline has not been trained yet.");
		}
		ArrayList<Attribute> atts = new ArrayList<>();
		List<String> attributeValues = WekaUtil.getClassesDeclaredInDataset(this.emptyReferenceDataset);
		for (String att : attributeValues) {
			atts.add(new Attribute("probe_classprob_" + att + "_" + this));
		}
		return new Instances("probing", atts, 0);
	}

	@Override
	public Instance apply(final Instance data) throws PreprocessingException {
		double[] classProbs;
		try {
			classProbs = this.distributionForInstance(data);
			Instance newInst = new DenseInstance(classProbs.length);
			Instances dataset = this.getEmptyProbingResultDataset();
			dataset.add(newInst);
			newInst.setDataset(dataset);
			for (int i = 0; i < classProbs.length; i++) {
				newInst.setValue(i, classProbs[i]);
			}
			return newInst;
		} catch (Exception e) {
			throw new PreprocessingException(e);
		}
	}

	@Override
	public Instances apply(final Instances data) throws PreprocessingException{
		Instances probingResults = new Instances(this.getEmptyProbingResultDataset());
		for (Instance inst : data) {
			Instance probedInst = this.apply(inst);
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
