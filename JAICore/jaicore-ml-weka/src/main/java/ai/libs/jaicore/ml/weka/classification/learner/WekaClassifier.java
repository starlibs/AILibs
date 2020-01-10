package ai.libs.jaicore.ml.weka.classification.learner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.serialization.UnsupportedAttributeTypeException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.LearnerConfigurationFailedException;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.common.reconstruction.IReconstructible;
import org.api4.java.common.reconstruction.IReconstructionInstruction;
import org.api4.java.common.reconstruction.IReconstructionPlan;

import ai.libs.jaicore.basic.reconstruction.ReconstructionInstruction;
import ai.libs.jaicore.basic.reconstruction.ReconstructionPlan;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassification;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassificationPredictionBatch;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.classification.pipeline.MLPipeline;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstance;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstance;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.OptionHandler;

public class WekaClassifier extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, ISingleLabelClassification, ISingleLabelClassificationPredictionBatch> implements IWekaClassifier, IReconstructible {

	private final String name;
	private Classifier wrappedClassifier;

	private ILabeledInstanceSchema schema;

	public static WekaClassifier createPipeline(final String searcher, final List<String> searcherOptions, final String evaluator, final List<String> evaluatorOptions, final String classifier, final List<String> classifierOptions)
			throws Exception {
		ASSearch search = ASSearch.forName(searcher, searcherOptions.toArray(new String[0]));
		ASEvaluation eval = ASEvaluation.forName(evaluator, evaluatorOptions.toArray(new String[0]));
		Classifier c = AbstractClassifier.forName(classifier, classifierOptions.toArray(new String[0]));
		return new WekaClassifier(new MLPipeline(search, eval, c));
	}

	public static WekaClassifier createBaseClassifier(final String name, final List<String> options) {
		return new WekaClassifier(name, options.toArray(new String[0]));
	}

	public WekaClassifier(final String name, final String[] options) {
		this.name = name;
		try {
			this.wrappedClassifier = AbstractClassifier.forName(name, options);
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not find classifier for name " + name + " or could not set its options to " + Arrays.toString(options), e);
		}
	}

	public WekaClassifier(final Classifier classifier) {
		this.wrappedClassifier = classifier;
		this.name = classifier.getClass().getName();
	}

	public String getName() {
		return this.name;
	}

	public String[] getOptions() {
		return ((OptionHandler) this.wrappedClassifier).getOptions();
	}

	@Override
	public void fit(final ILabeledDataset<? extends ILabeledInstance> dTrain) throws TrainingException, InterruptedException {
		this.schema = dTrain.getInstanceSchema();
		WekaInstances data = new WekaInstances(dTrain);

		try {
			this.wrappedClassifier.buildClassifier(data.getInstances());
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new TrainingException("Could not build " + this.getClass().getSimpleName() + " due to exception", e);
		}

	}

	@Override
	public ISingleLabelClassification predict(final ILabeledInstance xTest) throws PredictionException, InterruptedException {
		if (this.schema == null) {
			throw new IllegalStateException("Cannot conduct predictions with the classifier, because the dataset scheme has not been defined.");
		}
		WekaInstance instance;
		if (xTest instanceof WekaInstance) {
			instance = (WekaInstance) xTest;
		} else {
			try {
				instance = new WekaInstance(this.schema, xTest);
			} catch (UnsupportedAttributeTypeException e) {
				throw new PredictionException("Could not create WekaInstance object from given instance.");
			}
		}

		try {
			Map<Integer, Double> distribution = new HashMap<>();
			double[] dist = this.wrappedClassifier.distributionForInstance(instance.getElement());
			IntStream.range(0, dist.length).forEach(x -> distribution.put(x, dist[x]));
			return new SingleLabelClassification(distribution);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new PredictionException("Could not make a prediction since an exception occurred in the wrapped weka classifier.", e);
		}
	}

	@Override
	public ISingleLabelClassificationPredictionBatch predict(final ILabeledDataset<? extends ILabeledInstance> dTest) throws PredictionException, InterruptedException {
		IWekaInstances wInstances = new WekaInstances(dTest);
		int n = dTest.size();
		IWekaInstance[] instances = new IWekaInstance[n];
		for (int i = 0; i < n; i++) {
			instances[i] = wInstances.get(i);
		}
		return this.predict(instances);
	}

	@Override
	public ISingleLabelClassificationPredictionBatch predict(final ILabeledInstance[] dTest) throws PredictionException, InterruptedException {
		List<ISingleLabelClassification> predictions = new ArrayList<>();
		for (ILabeledInstance inst : dTest) {
			predictions.add(this.predict(inst));
		}
		return new SingleLabelClassificationPredictionBatch(predictions);
	}

	@Override
	public void setConfig(final Map<String, Object> config) throws LearnerConfigurationFailedException, InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Classifier getClassifier() {
		return this.wrappedClassifier;
	}

	@Override
	public IReconstructionPlan getConstructionPlan() {
		try {
			if (this.wrappedClassifier instanceof MLPipeline) {
				MLPipeline pipeline = (MLPipeline) this.wrappedClassifier;
				Classifier classifier = pipeline.getBaseClassifier();
				ASSearch searcher = pipeline.getPreprocessors().get(0).getSearcher();
				ASEvaluation evaluator = pipeline.getPreprocessors().get(0).getEvaluator();
				return new ReconstructionPlan(
						Arrays.asList(new ReconstructionInstruction(WekaClassifier.class.getMethod("createPipeline", String.class, List.class, String.class, List.class, String.class, List.class), searcher.getClass().getName(),
								((OptionHandler) searcher).getOptions(), evaluator.getClass().getName(), ((OptionHandler) evaluator).getOptions(), classifier.getClass().getName(), ((OptionHandler) classifier).getOptions())));
			} else {
				return new ReconstructionPlan(Arrays.asList(new ReconstructionInstruction(WekaClassifier.class.getMethod("createBaseClassifier", String.class, List.class), this.name, this.getOptionsAsList())));
			}
		} catch (NoSuchMethodException | SecurityException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	public List<String> getOptionsAsList() {
		return Arrays.asList(((OptionHandler) this.wrappedClassifier).getOptions());
	}

	@Override
	public void addInstruction(final IReconstructionInstruction instruction) {
		throw new UnsupportedOperationException("The WEKAClassifier cannot be modified afterwards, so no new instruction makes sense.");
	}

	@Override
	public String toString() {
		String c = this.wrappedClassifier instanceof MLPipeline ? this.wrappedClassifier.toString() : WekaUtil.getClassifierDescriptor(this.wrappedClassifier);
		return "WekaClassifier [name=" + this.name + ", options=" + this.getOptionsAsList() + ", wrappedClassifier=" + c + "]";
	}
}
