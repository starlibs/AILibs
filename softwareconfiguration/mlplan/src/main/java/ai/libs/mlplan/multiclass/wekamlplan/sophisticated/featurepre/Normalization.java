package ai.libs.mlplan.multiclass.wekamlplan.sophisticated.featurepre;

import ai.libs.mlplan.multiclass.wekamlplan.sophisticated.FeaturePreprocessor;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

public class Normalization implements FeaturePreprocessor {
	
	private weka.filters.unsupervised.attribute.Normalize norm = new weka.filters.unsupervised.attribute.Normalize();
	private boolean prepared;
	
	@Override
	public void prepare(Instances data) throws Exception {
		norm.setInputFormat(data);
		Filter.useFilter(data, norm);
		prepared = true;
	}

	@Override
	public Instance apply(Instance data) throws Exception {
		norm.input(data);
		return norm.output();
	}

	@Override
	public Instances apply(Instances data) throws Exception {
		Instances newInstances = new Instances(data);
		newInstances.clear();
		for (Instance i : data) {
			newInstances.add(apply(i));
		}
		return newInstances;
	}

	@Override
	public boolean isPrepared() {
		return prepared;
	}

}
