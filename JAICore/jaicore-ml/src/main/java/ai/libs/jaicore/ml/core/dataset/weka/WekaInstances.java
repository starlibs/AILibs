package ai.libs.jaicore.ml.core.dataset.weka;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.api4.java.ai.ml.dataset.DatasetCreationException;
import org.api4.java.ai.ml.dataset.attribute.IAttributeType;
import org.api4.java.ai.ml.dataset.supervised.INumericFeatureSupervisedDataset;

import ai.libs.jaicore.basic.sets.ListDecorator;
import weka.core.Instance;
import weka.core.Instances;

public class WekaInstances extends ListDecorator<Instances, Instance, WekaInstance> implements INumericFeatureSupervisedDataset<Double, WekaInstance> {

	private final List<IAttributeType> featureTypes = new ArrayList<>();
	private final List<IAttributeType> labelTypes;

	public WekaInstances(final Instances list) throws ClassNotFoundException {
		super(list);
		int targetIndex = list.classIndex();
		if (targetIndex < 0) {
			throw new IllegalArgumentException("Class index of Instances object is not set!");
		}

		int numAttributes = list.numAttributes();
		for (int i = 0; i < numAttributes; i++) {
			this.featureTypes.add(WekaInstancesUtil.transformWEKAAttributeToAttributeType(list.attribute(i)));
		}

		this.labelTypes = new LinkedList<>();
		this.labelTypes.add(this.featureTypes.remove(targetIndex));
	}

	@Override
	public WekaInstances createEmptyCopy() throws DatasetCreationException {
		try {
			return new WekaInstances(new Instances(this.getList(), 0));
		} catch (ClassNotFoundException e) {
			throw new DatasetCreationException(e);
		}
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hb = new HashCodeBuilder();
		for (WekaInstance inst : this) {
			hb.append(inst.hashCode());
		}
		return hb.toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		WekaInstances other = (WekaInstances) obj;
		int n = this.size();
		for (int i = 0; i < n; i++) {
			if (!this.get(i).equals(other.get(i))) {
				return false;
			}
		}
		return true;
	}

	public int getFrequency(final WekaInstance instance) {
		return (int) this.stream().filter(instance::equals).count();
	}

	@Override
	public String toString() {
		return "WekaInstances [targetType=" + this.labelTypes + ", attributeTypes=" + this.featureTypes + "]\n" + this.getList();
	}

	@Override
	public List<IAttributeType> getLabelTypes() {
		return this.labelTypes;
	}

	@Override
	public int getNumLabels() {
		return this.labelTypes.size();
	}

	@Override
	public List<IAttributeType> getFeatureTypes() {
		return this.featureTypes;
	}

	@Override
	public int getNumFeatures() {
		return this.featureTypes.size();
	}
}
