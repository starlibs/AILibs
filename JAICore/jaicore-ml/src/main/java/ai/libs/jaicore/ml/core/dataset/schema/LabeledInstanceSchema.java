package ai.libs.jaicore.ml.core.dataset.schema;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;

public class LabeledInstanceSchema extends InstanceSchema implements ILabeledInstanceSchema {

	private IAttribute labelAttribute;

	public LabeledInstanceSchema(final String relationName, final List<IAttribute> attributeList, final IAttribute labelAttribute) {
		super(relationName, attributeList);
		this.labelAttribute = labelAttribute;
	}

	@Override
	public IAttribute getLabelAttribute() {
		return this.labelAttribute;
	}

	@Override
	public void replaceLabelAttribute(final IAttribute labelAttribute) {
		this.labelAttribute = labelAttribute;
	}

	@Override
	public LabeledInstanceSchema getCopy() {
		return new LabeledInstanceSchema(this.getRelationName(), new ArrayList<>(this.getAttributeList()), this.labelAttribute);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(super.toString());
		sb.append("\n");
		sb.append("Target: ");
		sb.append(this.labelAttribute);

		return sb.toString();
	}

}
