package ai.libs.jaicore.ml.classification.multilabel.learner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;
import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.serialization.UnsupportedAttributeTypeException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.common.reconstruction.IReconstructible;
import org.api4.java.common.reconstruction.IReconstructionInstruction;
import org.api4.java.common.reconstruction.IReconstructionPlan;

import ai.libs.jaicore.basic.reconstruction.ReconstructionPlan;
import ai.libs.jaicore.ml.classification.multilabel.MultiLabelClassification;
import ai.libs.jaicore.ml.classification.multilabel.MultiLabelClassificationPredictionBatch;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstance;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import meka.classifiers.multilabel.MultiLabelClassifier;

public class MekaClassifier extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, IMultiLabelClassification, IMultiLabelClassificationPredictionBatch> implements IMekaClassifier, IReconstructible {

	private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

	private MultiLabelClassifier classifier;
	private IReconstructionPlan reconstructionPlan;
	private ILabeledInstanceSchema schema;
	private final int id;

	public MekaClassifier(final MultiLabelClassifier classifier) {
		this.reconstructionPlan = new ReconstructionPlan();
		this.classifier = classifier;
		this.id = ID_COUNTER.getAndIncrement();
	}

	@Override
	public MultiLabelClassifier getClassifier() {
		return this.classifier;
	}

	@Override
	public void fit(final ILabeledDataset<? extends ILabeledInstance> dTrain) throws TrainingException, InterruptedException {
		this.schema = dTrain.getInstanceSchema();
		IWekaInstances dataset;
		if (dTrain instanceof WekaInstances) {
			dataset = (WekaInstances) dTrain;
		} else {
			dataset = new WekaInstances(dTrain);
		}
		try {
			this.classifier.buildClassifier(dataset.getInstances());
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new TrainingException("Could not build classifier.", e);
		}
	}

	@Override
	public IReconstructionPlan getConstructionPlan() {
		return this.reconstructionPlan;
	}

	@Override
	public void addInstruction(final IReconstructionInstruction instruction) {
		this.reconstructionPlan.getInstructions().add(instruction);
	}

	@Override
	public IMultiLabelClassification predict(final ILabeledInstance xTest) throws PredictionException, InterruptedException {
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
			double[] dist = this.classifier.distributionForInstance(instance.getElement());
			IntStream.range(0, dist.length).forEach(x -> distribution.put(x, dist[x]));
			return new MultiLabelClassification(dist);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new PredictionException("Could not make a prediction since an exception occurred in the wrapped weka classifier.", e);
		}
	}

	@Override
	public IMultiLabelClassificationPredictionBatch predict(final ILabeledInstance[] dTest) throws PredictionException, InterruptedException {
		List<IMultiLabelClassification> batch = new ArrayList<>();
		for (ILabeledInstance instance : dTest) {
			batch.add(this.predict(instance));
		}
		return new MultiLabelClassificationPredictionBatch(batch);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("#");
		sb.append(this.id);
		sb.append(": ");
		sb.append(WekaUtil.getClassifierDescriptor(this.classifier));
		return sb.toString();
	}

}
