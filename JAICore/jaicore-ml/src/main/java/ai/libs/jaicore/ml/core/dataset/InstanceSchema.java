package ai.libs.jaicore.ml.core.dataset;

import java.io.Serializable;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;

@SuppressWarnings("serial")
public class InstanceSchema implements Serializable {

	private final List<IAttribute> attributeTypeList;
	private final IAttribute targetType;

	public InstanceSchema(final List<IAttribute> attributeTypeList, final IAttribute targetType) {
		this.attributeTypeList = attributeTypeList;
		this.targetType = targetType;
	}

	public List<IAttribute> getAttributeTypeList() {
		return this.attributeTypeList;
	}

	public IAttribute get(final int index) {
		return this.attributeTypeList.get(index);
	}

	public IAttribute getTargetType() {
		return this.targetType;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("%attributes\n");

		for (IAttribute t : this.attributeTypeList) {
			sb.append(t.toString() + "\n");
		}
		sb.append("%target\n");
		sb.append(this.targetType.toString());
		return sb.toString();
	}

}
