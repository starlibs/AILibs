package ai.libs.jaicore.ml.core.dataset.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.schema.IInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;

public class InstanceSchema implements IInstanceSchema {

	private String relationName;
	private final List<IAttribute> attributeList;

	public InstanceSchema(final String relationName, final Collection<IAttribute> attributeList) {
		this.relationName = relationName;
		this.attributeList = new ArrayList<>(attributeList);
	}

	@Override
	public List<IAttribute> getAttributeList() {
		return this.attributeList;
	}

	@Override
	public IAttribute getAttribute(final int pos) {
		return this.attributeList.get(pos);
	}

	@Override
	public int getNumAttributes() {
		return this.attributeList.size();
	}

	@Override
	public String getRelationName() {
		return this.relationName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("%attributes\n");
		for (IAttribute t : this.attributeList) {
			sb.append(t.toString() + "\n");
		}
		return sb.toString();
	}

	@Override
	public void removeAttribute(final int columnPos) {
		this.attributeList.remove(columnPos);
	}

	@Override
	public void addAttribute(final int pos, final IAttribute attribute) {
		this.attributeList.add(pos, attribute);
	}

	@Override
	public void addAttribute(final IAttribute attribute) {
		this.attributeList.add(attribute);
	}

	@Override
	public IInstanceSchema getCopy() {
		return new InstanceSchema(relationName, new ArrayList<>(attributeList));
	}

}
