package ai.libs.mlplan.core;

import java.util.List;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

public interface ITimeTrackingLearner extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> {

	public List<Long> getFitTimes();

	public List<Long> getBatchPredictionTimes();

	public List<Long> getInstancePredictionTimes();

}
