package ai.libs.jaicore.ml.weka.classification.learner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.exception.LearnerConfigurationFailedException;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.common.reconstruction.IReconstructible;
import org.api4.java.common.reconstruction.IReconstructionInstruction;
import org.api4.java.common.reconstruction.IReconstructionPlan;

import ai.libs.jaicore.basic.reconstruction.ReconstructionInstruction;
import ai.libs.jaicore.basic.reconstruction.ReconstructionPlan;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.classification.pipeline.MLPipeline;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstance;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.OptionHandler;

public abstract class AWekaLearner<P extends IPrediction, B extends IPredictionBatch> extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, P, B> implements IWekaClassifier, IReconstructible {

	protected String name;
	protected Classifier wrappedLearner;

	protected ILabeledInstanceSchema schema;

	public AWekaLearner(final Classifier learner) {
		this.wrappedLearner = learner;
	}

	public AWekaLearner(final String name, final String[] options) {
		this.name = name;
		try {
			this.wrappedLearner = AbstractClassifier.forName(name, options);
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not find classifier for name " + name + " or could not set its options to " + Arrays.toString(options), e);
		}
	}

	@Override
	public void fit(final ILabeledDataset<? extends ILabeledInstance> dTrain) throws TrainingException, InterruptedException {
		this.schema = dTrain.getInstanceSchema();
		WekaInstances data = new WekaInstances(dTrain);

		try {
			this.wrappedLearner.buildClassifier(data.getInstances());
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new TrainingException("Could not build " + this.getClass().getSimpleName() + " due to exception", e);
		}

	}

	@Override
	public B predict(final ILabeledDataset<? extends ILabeledInstance> dTest) throws PredictionException, InterruptedException {
		IWekaInstances wInstances = new WekaInstances(dTest);
		int n = dTest.size();
		IWekaInstance[] instances = new IWekaInstance[n];
		for (int i = 0; i < n; i++) {
			instances[i] = wInstances.get(i);
		}
		return this.predict(instances);
	}

	@Override
	public Classifier getClassifier() {
		return this.wrappedLearner;
	}

	public String getName() {
		return this.name;
	}

	public String[] getOptions() {
		return ((OptionHandler) this.wrappedLearner).getOptions();
	}

	public List<String> getOptionsAsList() {
		return Arrays.asList(((OptionHandler) this.wrappedLearner).getOptions());
	}

	@Override
	public void addInstruction(final IReconstructionInstruction instruction) {
		throw new UnsupportedOperationException("The WEKAClassifier cannot be modified afterwards, so no new instruction makes sense.");
	}

	@Override
	public IReconstructionPlan getConstructionPlan() {
		try {
			if (this.wrappedLearner instanceof MLPipeline) {
				MLPipeline pipeline = (MLPipeline) this.wrappedLearner;
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

	@Override
	public void setConfig(final Map<String, Object> config) throws LearnerConfigurationFailedException, InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		String c = this.wrappedLearner instanceof MLPipeline ? this.wrappedLearner.toString() : WekaUtil.getClassifierDescriptor(this.wrappedLearner);
		return "WekaClassifier [name=" + this.name + ", options=" + this.getOptionsAsList() + ", wrappedClassifier=" + c + "]";
	}

	@Override
	public B predict(final ILabeledInstance[] dTest) throws PredictionException, InterruptedException {
		List<P> predictions = new ArrayList<>();
		for (ILabeledInstance inst : dTest) {
			predictions.add(this.predict(inst));
		}
		return this.getPredictionListAsBatch(predictions);
	}

	protected abstract B getPredictionListAsBatch(List<P> predictionList);
}
