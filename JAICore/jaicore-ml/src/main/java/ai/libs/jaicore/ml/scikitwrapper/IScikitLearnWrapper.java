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

/**
 * Handles the execution of a scikit-learn pipeline in python and makes the according predictions available. A scikit-learn pipeline is a composition of one or multiple (ML) algorithms.
 *
 * @see <a href="https://scikit-learn.org/stable/modules/compose.html">scikit-learn: Pipelines and composite estimators</a>
 *
 * @author tornede
 *
 */
public interface IScikitLearnWrapper extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>, ILoggingCustomizable {

	public void setPythonTemplate(final String pythonTemplatePath) throws IOException;

	public void setModelPath(final String modelPath) throws IOException;

	public File getModelPath();

	public void setSeed(final long seed);

	public void setTimeout(final Timeout timeout);

	public void fit(final String trainingDataName) throws TrainingException, InterruptedException;

	public String getDataName(final ILabeledDataset<? extends ILabeledInstance> data);

	public File getOutputFile(final String dataName);

	public void setPythonConfig(final IPythonConfig pythonConfig) throws IOException, InterruptedException;

	public void setScikitLearnWrapperConfig(final IScikitLearnWrapperConfig scikitLearnWrapperConfig);

	public File getSKLearnScriptFile();

	public File getModelFile();

	public void setTargetIndices(int... targetIndices);

}
