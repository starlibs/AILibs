package ai.libs.jaicore.ml.weka.classification.pipeline.featurepreprocess;

import ai.libs.jaicore.ml.weka.classification.pipeline.FeaturePreprocessor;
import ai.libs.jaicore.ml.weka.classification.pipeline.PreprocessingException;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

public class Standardization implements FeaturePreprocessor {

	private static final long serialVersionUID = -6540039548736716606L;
	private weka.filters.unsupervised.attribute.Standardize stand = new weka.filters.unsupervised.attribute.Standardize();
	private boolean prepared;

	@Override
	public void prepare(final Instances data) throws PreprocessingException {
		try {
			this.stand.setInputFormat(data);
			this.stand.setIgnoreClass(true);
			Filter.useFilter(data, this.stand);
			this.prepared = true;
		} catch (Exception e) {
			throw new PreprocessingException(e);
		}
	}

	@Override
	public Instance apply(final Instance data) throws PreprocessingException {
		try {
			this.stand.input(data);
			return this.stand.output();
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
