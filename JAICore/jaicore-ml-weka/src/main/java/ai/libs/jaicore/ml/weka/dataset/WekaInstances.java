package ai.libs.jaicore.ml.weka.dataset;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

import ai.libs.jaicore.basic.sets.ListDecorator;
import weka.core.Instance;
import weka.core.Instances;

public class WekaInstances extends ListDecorator<Instances, Instance, WekaInstance> implements ILabeledDataset<WekaInstance> {

	private final List<IAttribute> featureTypes = new ArrayList<>();
	private final List<IAttribute> labelTypes;

	public WekaInstances(final Instances list) {
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
		return new WekaInstances(new Instances(this.getList(), 0));
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
	public ILabeledInstanceSchema getInstanceSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[][] getFeatureMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getLabelVector() {
		// TODO Auto-generated method stub
		return null;
	}
}
