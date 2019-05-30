package jaicore.ml.core.dataset.weka;

import java.util.ArrayList;
import java.util.List;

import jaicore.basic.sets.ListDecorator;
import jaicore.ml.core.dataset.DatasetCreationException;
import jaicore.ml.core.dataset.IOrderedLabeledAttributeArrayDataset;
import jaicore.ml.core.dataset.attribute.IAttributeType;
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
}
