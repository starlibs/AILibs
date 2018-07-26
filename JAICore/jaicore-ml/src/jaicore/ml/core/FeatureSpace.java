package jaicore.ml.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class FeatureSpace implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4130427099174860007L;
	private List<FeatureDomain> featureDomains;

	public FeatureSpace() {
		featureDomains = new ArrayList<>();
	}

	public FeatureSpace(Instances data) {
		this();
		for (int i = 0; i < data.numAttributes(); i++) {
			Attribute attr = data.attribute(i);
			if (data.classIndex() == i) {
				continue;
			}
			if (attr.isNumeric()) {
				double min, max;
				if (attr.getLowerNumericBound() < attr.getLowerNumericBound()) {
					min = attr.getLowerNumericBound();
					max = attr.getUpperNumericBound();
				}
				// if no range is specified, use minimum and maximum of datapoints
				else {
					min = data.attributeStats(i).numericStats.min;
					max = data.attributeStats(i).numericStats.max;
				}
				NumericFeatureDomain domain = new NumericFeatureDomain(false, min, max);

				domain.setName(attr.name());
				featureDomains.add(domain);
			} else if (attr.isNominal()) {
				String[] attrVals = new String[attr.numValues()];
				double[] internalVals = new double[attr.numValues()];
				for (int valIndex = 0; valIndex < attr.numValues(); valIndex++) {
					attrVals[valIndex] = attr.value(valIndex);
					internalVals[valIndex] = (double) valIndex;
				}
				CategoricalFeatureDomain domain = new CategoricalFeatureDomain(internalVals);
				domain.setName(attr.name());
				featureDomains.add(domain);
			} else {
				throw new IllegalArgumentException("Attribute type not supported!");
			}
		}
	}

	/**
	 * copy constructor
	 * 
	 * @param domains
	 */
	public FeatureSpace(List<FeatureDomain> domains) {
		featureDomains = new ArrayList<FeatureDomain>();
		for (FeatureDomain domain : domains) {
			if (domain instanceof NumericFeatureDomain) {
				NumericFeatureDomain numDomain = (NumericFeatureDomain) domain;
				featureDomains.add(new NumericFeatureDomain(numDomain));
			} else if (domain instanceof CategoricalFeatureDomain) {
				CategoricalFeatureDomain catDomain = (CategoricalFeatureDomain) domain;
				featureDomains.add(new CategoricalFeatureDomain(catDomain));
			}
		}
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
		return featureDomains.toArray(new FeatureDomain[featureDomains.size()]);
	}

	public double getRangeSize() {
		double size = 1.0d;
		for (FeatureDomain domain : featureDomains)
			size *= domain.getRangeSize();
		return size;
	}

	public double getRangeSizeOfFeatureSubspace(Set<Integer> featureIndices) {
		double size = 1.0d;
		for (int featureIndex : featureIndices)
			size *= featureDomains.get(featureIndex).getRangeSize();
		return size;
	}

	public double getRangeSizeOfAllButSubset(Set<Integer> featureIndices) {
		double size = 1.0d;
		for (int i = 0; i < this.getDimensionality(); i++) {
			if (featureIndices.contains(i))
				continue;
			else
				size *= featureDomains.get(i).getRangeSize();
		}
		return size;
	}

	public int getDimensionality() {
		return featureDomains.size();
	}

	public FeatureDomain getFeatureDomain(int index) {
		return featureDomains.get(index);
	}

	public boolean containsPartialInstance(List<Integer> indices, List<Double> values) {
		for(int i = 0; i < indices.size(); i++) {
			int featureIndex = indices.get(i);
			double value = values.get(i);
			if(!featureDomains.get(featureIndex).containsInstance(value))
				return false;
		}
		return true;
	}
	
	public boolean containsInstance(Instance instance) {
		boolean val = true;
		for (int i = 0; i < featureDomains.size(); i++) {
			FeatureDomain domain = featureDomains.get(i);
			val &= domain.contains(instance.value(i));
		}
		return val;
	}

}
