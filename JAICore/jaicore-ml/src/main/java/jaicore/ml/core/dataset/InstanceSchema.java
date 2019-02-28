package jaicore.ml.core.dataset;

import java.util.List;

import jaicore.ml.core.dataset.attribute.IAttributeType;

public class InstanceSchema {

	private final List<IAttributeType<?>> attributeTypeList;
	private final IAttributeType<?> targetType;

	public InstanceSchema(final List<IAttributeType<?>> attributeTypeList, final IAttributeType<?> targetType) {
		this.attributeTypeList = attributeTypeList;
		this.targetType = targetType;
	}

	public List<IAttributeType<?>> getAttributeTypeList() {
		return this.attributeTypeList;
	}

	public IAttributeType<?> get(final int index) {
		return this.attributeTypeList.get(index);
	}

	public <T> IAttributeType<T> getTargetType(final Class<T> type) {
		return (IAttributeType<T>) this.targetType;
	}

	public IAttributeType<?> getTargetType() {
		return this.targetType;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("%attributes\n");

		for (IAttributeType<?> t : this.attributeTypeList) {
			sb.append(t.toString() + "\n");
		}
		sb.append("%target\n");
		sb.append(this.targetType.toString());
		return sb.toString();
	}

}
