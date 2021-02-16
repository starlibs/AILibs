package ai.libs.jaicore.ml.scikitwrapper;

import java.io.File;
import java.io.IOException;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.Timeout;
import org.api4.java.common.control.ILoggingCustomizable;

import ai.libs.python.IPythonConfig;

public interface IScikitLearnWrapper extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>, ILoggingCustomizable {

	public void setPythonTemplate(final String pythonTemplatePath) throws IOException;

	public void setModelPath(final String modelPath) throws IOException;

	public File getModelPath();

	public void setSeed(final long seed);

	public void setTimeout(final Timeout timeout);

	// public void setTargets(final int... targetColumns) {
	// this.targetColumns = targetColumns;
	// }

	public void fit(final String trainingDataName) throws TrainingException, InterruptedException;

	public String getDataName(final ILabeledDataset<? extends ILabeledInstance> data);

	public File getOutputFile(final String dataName);

	public void setPythonConfig(final IPythonConfig pythonConfig);

	public void setScikitLearnWrapperConfig(final IScikitLearnWrapperConfig scikitLearnWrapperConfig);

	public File getSKLearnScriptFile();

	public File getModelFile();

}
