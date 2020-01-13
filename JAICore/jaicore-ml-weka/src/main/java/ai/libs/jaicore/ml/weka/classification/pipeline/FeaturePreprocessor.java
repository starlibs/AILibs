package ai.libs.jaicore.ml.weka.classification.pipeline;

import java.io.Serializable;

import weka.core.Instance;
import weka.core.Instances;

public interface FeaturePreprocessor extends Serializable {

	public void prepare(Instances data) throws PreprocessingException;

	public Instance apply(Instance data) throws PreprocessingException;

	public Instances apply(Instances data) throws PreprocessingException;

	public boolean isPrepared();
}
