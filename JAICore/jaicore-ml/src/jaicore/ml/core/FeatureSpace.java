package jaicore.ml.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import weka.core.Instance;

public class FeatureSpace {
	
	private List<FeatureDomain> featureDomains;
	
	public FeatureSpace() {
		featureDomains = new ArrayList<>();
	}
	
	public FeatureSpace(List<FeatureDomain> domains) {
		featureDomains = new ArrayList<FeatureDomain>(domains);
	}
	
	public FeatureSpace(FeatureSpace space) {
		this(Arrays.asList(space.getFeatureDomains()));
	}
	
	public FeatureSpace(FeatureDomain[] domains) {
		this(Arrays.asList(domains));
	}
	
	public FeatureDomain[] toArray() {
		return (FeatureDomain[]) featureDomains.toArray();
	}
	
	public void add(FeatureDomain domain) {
		featureDomains.add(domain);
	}
	
	public FeatureDomain[] getFeatureDomains() {
		return (FeatureDomain[]) featureDomains.toArray();
	}
		
	public double getRangeSize() {
		double size = 1.0d;
		for(FeatureDomain domain: featureDomains)
			size *= domain.getRangeSize();
		return size;
	}
	
	public int getDimension() {
		return featureDomains.size();
	}
	
	public FeatureDomain getFeatureDomain(int index) {
		return featureDomains.get(index);
	}
	
	public boolean containsInstance(Instance instance) {
		boolean val = true;
		for(int i = 0; i < featureDomains.size(); i++) {
			FeatureDomain domain = featureDomains.get(i);
			val &= domain.contains(instance.value(i));
		}
		return val;
	}
}
