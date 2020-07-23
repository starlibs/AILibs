package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import org.api4.java.ai.ml.core.dataset.splitter.IDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

public interface ISplitBasedSupervisedLearnerEvaluatorFactory<I extends ILabeledInstance, D extends ILabeledDataset<? extends I>, F> extends ISupervisedLearnerEvaluatorFactory<I, D> {

	/**
	 * Sets the dataset spliter to the given dataset splitter.
	 * @param datasetSplitter The dataset splitter to be used for splitting the dataset into train and test folds.
	 * @return The instance of the factory.
	 */
	public F withDatasetSplitter(IDatasetSplitter<? extends ILabeledDataset<?>> datasetSplitter);

	/**
	 * @return The currently configured dataset splitter.
	 */
	public IDatasetSplitter<? extends ILabeledDataset<?>> getDatasetSplitter();

}
