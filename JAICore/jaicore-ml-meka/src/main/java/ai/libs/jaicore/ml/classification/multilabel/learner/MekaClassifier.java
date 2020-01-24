package ai.libs.jaicore.ml.classification.multilabel.learner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.reconstruction.ReconstructionPlan;
import ai.libs.jaicore.ml.classification.multilabel.MultiLabelClassification;
import ai.libs.jaicore.ml.classification.multilabel.MultiLabelClassificationPredictionBatch;
import ai.libs.jaicore.ml.classification.multilabel.dataset.IMekaInstances;
import ai.libs.jaicore.ml.classification.multilabel.dataset.MekaInstance;
import ai.libs.jaicore.ml.classification.multilabel.dataset.MekaInstances;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;
import ai.libs.jaicore.ml.weka.WekaUtil;
import meka.classifiers.multilabel.MultiLabelClassifier;
import weka.core.DenseInstance;

public class MekaClassifier extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, IMultiLabelClassification, IMultiLabelClassificationPredictionBatch> implements IMekaClassifier, IReconstructible {

	private static final Logger LOGGER = LoggerFactory.getLogger(MekaClassifier.class);

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
		LOGGER.debug("Obtain instance schema from training instances.");
		this.schema = dTrain.getInstanceSchema();

		LOGGER.debug("Ensure instances to be of the correct format.");
		IMekaInstances dataset;
		if (dTrain instanceof IMekaInstances) {
			LOGGER.debug("Instances are already of type IMekaInstances so just perform a type cast.");
			dataset = (IMekaInstances) dTrain;
		} else {
			LOGGER.debug("Instances are not of type IMekaInstances so make them MekaInstances.");
			dataset = new MekaInstances(dTrain);
		}

		LOGGER.debug("Build the classifier");
		try {
			this.classifier.buildClassifier(dataset.getInstances());
			LOGGER.debug("Done building the classifier.");
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
		MekaInstance instance;
		if (xTest instanceof MekaInstance) {
			instance = (MekaInstance) xTest;
		} else {
			try {
				instance = new MekaInstance(this.schema, xTest);
			} catch (UnsupportedAttributeTypeException e) {
				throw new PredictionException("Could not create WekaInstance object from given instance.");
			}
		}

		try {
			// XXX: Work Around: Better make a fresh copy of that instance because some MEKA classifiers might change the information contained in that instance.
			DenseInstance copy = new DenseInstance(instance.getElement());
			copy.setDataset(instance.getElement().dataset());
			double[] dist = this.classifier.distributionForInstance(copy);
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
