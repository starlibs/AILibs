package jaicore.ml.core.dataset.weka;

import java.util.ArrayList;
import java.util.List;

import jaicore.basic.sets.ListDecorator;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.attribute.IAttributeType;
import weka.core.Instance;
import weka.core.Instances;

public class WekaInstances extends ListDecorator<Instances, Instance, WekaInstance> implements IDataset<WekaInstance> {

	private final IAttributeType<?> targetType;
	private final List<IAttributeType<?>> attributeTypes = new ArrayList<>();

	public WekaInstances(final Instances list) {
		super(list);
		int targetIndex = list.classIndex();
		if (targetIndex < 0) {
			throw new IllegalArgumentException("Class index of Instances object is not set!");
		}

		int numAttributes = list.numAttributes();
		for (int i = 0; i < numAttributes; i++) {
			this.attributeTypes.add(WekaInstancesUtil.transformWEKAAttributeToAttributeType(list.attribute(i)));
		}
		this.targetType = this.attributeTypes.get(targetIndex);
		this.attributeTypes.remove(targetIndex);
	}

	@Override
	public <T> IAttributeType<T> getTargetType(final Class<T> clazz) {
		return (IAttributeType<T>)this.targetType;
	}

	@Override
	public IAttributeType<?> getTargetType() {
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
	public IDataset<WekaInstance> createEmpty() {
		return new WekaInstances(new Instances(this.getList(), 0));
	}
}
