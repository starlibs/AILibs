package ai.libs.jaicore.ml.weka.classification.pipeline.featurepreprocess;

import ai.libs.jaicore.ml.weka.classification.pipeline.FeaturePreprocessor;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

public class Normalization implements FeaturePreprocessor {

	private weka.filters.unsupervised.attribute.Normalize norm = new weka.filters.unsupervised.attribute.Normalize();
	private boolean prepared;

	@Override
	public void prepare(final Instances data) throws Exception {
		this.norm.setInputFormat(data);
		Filter.useFilter(data, this.norm);
		this.prepared = true;
	}

	@Override
	public Instance apply(final Instance data) throws Exception {
		this.norm.input(data);
		return this.norm.output();
	}

	@Override
	public Instances apply(final Instances data) throws Exception {
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
