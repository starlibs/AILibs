package jaicore.ml.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
		this.featureDomains = new ArrayList<>();
	}

	public FeatureSpace(final Instances data) {
		this();
		for (int i = 0; i < data.numAttributes(); i++) {
			Attribute attr = data.attribute(i);
			if (data.classIndex() == i) {
				continue;
			}
			if (attr.isNumeric()) {
				double min;
				double max;
				if (attr.getLowerNumericBound() < attr.getUpperNumericBound()) {
					min = attr.getLowerNumericBound();
					max = attr.getUpperNumericBound();
				}
				// if no range is specified, use minimum and maximum of datapoints
				else {
					min = data.attributeStats(i).numericStats.min;
					max = data.attributeStats(i).numericStats.max;
				}

				NumericFeatureDomain domain = new NumericFeatureDomain(true, min, max);

				domain.setName(attr.name());
				this.featureDomains.add(domain);
			} else if (attr.isNominal()) {
				String[] attrVals = new String[attr.numValues()];
				double[] internalVals = new double[attr.numValues()];
				for (int valIndex = 0; valIndex < attr.numValues(); valIndex++) {
					attrVals[valIndex] = attr.value(valIndex);
					internalVals[valIndex] = valIndex;
				}
				CategoricalFeatureDomain domain = new CategoricalFeatureDomain(internalVals);
				domain.setName(attr.name());
				this.featureDomains.add(domain);
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
	public FeatureSpace(final List<FeatureDomain> domains) {
		this.featureDomains = new ArrayList<>();
		for (FeatureDomain domain : domains) {
			if (domain instanceof NumericFeatureDomain) {
				NumericFeatureDomain numDomain = (NumericFeatureDomain) domain;
				this.featureDomains.add(new NumericFeatureDomain(numDomain));
			} else if (domain instanceof CategoricalFeatureDomain) {
				CategoricalFeatureDomain catDomain = (CategoricalFeatureDomain) domain;
				this.featureDomains.add(new CategoricalFeatureDomain(catDomain));
			}
		}
	}

	public FeatureSpace(final FeatureSpace space) {
		this(Arrays.asList(space.getFeatureDomains()));
	}

	public FeatureSpace(final FeatureDomain[] domains) {
		this(Arrays.asList(domains));
	}

	public FeatureDomain[] toArray() {
		return this.featureDomains.toArray(new FeatureDomain[0]);
	}

	public void add(final FeatureDomain domain) {
		this.featureDomains.add(domain);
	}

	public FeatureDomain[] getFeatureDomains() {
		return this.featureDomains.toArray(new FeatureDomain[this.featureDomains.size()]);
	}

	public double getRangeSize() {
		double size = 1.0d;
		for (FeatureDomain domain : this.featureDomains) {
			size *= domain.getRangeSize();
		}
		return size;
	}

	public double getRangeSizeOfFeatureSubspace(final Set<Integer> featureIndices) {
		double size = 1.0d;
		for (int featureIndex : featureIndices) {
			size *= this.featureDomains.get(featureIndex).getRangeSize();
		}
		return size;
	}

	public double getRangeSizeOfAllButSubset(final Set<Integer> featureIndices) {
		double size = 1.0d;
		for (int i = 0; i < this.getDimensionality(); i++) {
			if (!featureIndices.contains(i)) {
				size *= this.featureDomains.get(i).getRangeSize();
			}
		}
		return size;
	}

	public int getDimensionality() {
		return this.featureDomains.size();
	}

	public FeatureDomain getFeatureDomain(final int index) {
		return this.featureDomains.get(index);
	}

	public boolean containsPartialInstance(final List<Integer> indices, final List<Double> values) {
		for (int i = 0; i < indices.size(); i++) {
			int featureIndex = indices.get(i);
			double value = values.get(i);
			if (!this.featureDomains.get(featureIndex).containsInstance(value)) {
				return false;
			}
		}
		return true;
	}

	public boolean containsInstance(final Instance instance) {
		boolean val = true;
		for (int i = 0; i < this.featureDomains.size(); i++) {
			FeatureDomain domain = this.featureDomains.get(i);
			val &= domain.contains(instance.value(i));
		}
		return val;
	}

}
