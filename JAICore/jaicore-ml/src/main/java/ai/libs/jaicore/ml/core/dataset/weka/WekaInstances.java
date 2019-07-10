package ai.libs.jaicore.ml.core.dataset.weka;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.api4.java.ai.ml.DatasetCreationException;
import org.api4.java.ai.ml.IOrderedLabeledAttributeArrayDataset;
import org.api4.java.ai.ml.core.dataset.attribute.IAttributeType;

import ai.libs.jaicore.basic.sets.ListDecorator;
import weka.core.Instance;
import weka.core.Instances;

public class WekaInstances<L> extends ListDecorator<Instances, Instance, WekaInstance<L>> implements IOrderedLabeledAttributeArrayDataset<WekaInstance<L>, L> {

	private final IAttributeType<L> targetType;
	private final List<IAttributeType<?>> attributeTypes = new ArrayList<>();

	public WekaInstances(final Instances list) throws ClassNotFoundException {
		super(list);
		int targetIndex = list.classIndex();
		if (targetIndex < 0) {
			throw new IllegalArgumentException("Class index of Instances object is not set!");
		}

		int numAttributes = list.numAttributes();
		for (int i = 0; i < numAttributes; i++) {
			this.attributeTypes.add(WekaInstancesUtil.transformWEKAAttributeToAttributeType(list.attribute(i)));
		}
		this.targetType = (IAttributeType<L>) this.attributeTypes.get(targetIndex);
		this.attributeTypes.remove(targetIndex);
	}

	@Override
	public IAttributeType<L> getTargetType() {
		return this.targetType;
	}

	@Override
	public List<IAttributeType<?>> getAttributeTypes() {
		return this.attributeTypes;
	}

	@Override
	public int getNumberOfAttributes() {
		return this.attributeTypes.size();
	}

	@Override
	public WekaInstances<L> createEmpty() throws DatasetCreationException {
		try {
			return new WekaInstances<>(new Instances(this.getList(), 0));
		} catch (ClassNotFoundException e) {
			throw new DatasetCreationException(e);
		}
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hb = new HashCodeBuilder();
		for (WekaInstance<L> inst : this) {
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

	@Override
	public int getFrequency(final WekaInstance<L> instance) {
		return (int)this.stream().filter(instance::equals).count();
	}

	@Override
	public String toString() {
		return "WekaInstances [targetType=" + this.targetType + ", attributeTypes=" + this.attributeTypes + "]\n" + this.getList();
	}
}
