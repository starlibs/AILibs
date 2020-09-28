package ai.libs.mlplan.core;

import java.util.List;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

import ai.libs.jaicore.components.api.IComponentInstance;

public interface ITimeTrackingLearner extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> {

	public ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> getLearner();

	public List<Long> getFitTimes();

	public List<Long> getBatchPredictionTimesInMS();

	public List<Long> getInstancePredictionTimesInMS();

	public IComponentInstance getComponentInstance();

	public void setPredictedInductionTime(final String inductionTime);

	public void setPredictedInferenceTime(final String inferenceTime);

	public Double getPredictedInductionTime();

	public Double getPredictedInferenceTime();

	public void setScore(Double score);

	public Double getScore();

}
