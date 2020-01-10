package ai.libs.jaicore.ml.weka.classification.pipeline.featurepreprocess;

import ai.libs.jaicore.ml.weka.classification.pipeline.FeaturePreprocessor;
import ai.libs.jaicore.ml.weka.classification.pipeline.PreprocessingException;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

public class Normalization implements FeaturePreprocessor {

	private static final long serialVersionUID = 3410424109277796158L;
	private weka.filters.unsupervised.attribute.Normalize norm = new weka.filters.unsupervised.attribute.Normalize();
	private boolean prepared;

	@Override
	public void prepare(final Instances data) throws PreprocessingException {
		try {
			this.norm.setInputFormat(data);
			Filter.useFilter(data, this.norm);
			this.prepared = true;
		} catch (Exception e) {
			throw new PreprocessingException(e);
		}
	}

	@Override
	public Instance apply(final Instance data) throws PreprocessingException {
		try {
			this.norm.input(data);
			return this.norm.output();
		} catch (Exception e) {
			throw new PreprocessingException(e);
		}
	}

	@Override
	public Instances apply(final Instances data) throws PreprocessingException {
		Instances newInstances = new Instances(data);
		newInstances.clear();
		for (Instance i : data) {
			newInstances.add(this.apply(i));
		}
		return newInstances;
	}

	@Override
	public boolean isPrepared() {
		return this.prepared;
	}

}
