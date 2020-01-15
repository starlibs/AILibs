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

		sb.append("Relation: " + this.relationName);
		sb.append("\n");
		sb.append("Features: " + this.attributeList);
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
		return new InstanceSchema(this.relationName, new ArrayList<>(this.attributeList));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.attributeList == null) ? 0 : this.attributeList.hashCode());
		result = prime * result + ((this.relationName == null) ? 0 : this.relationName.hashCode());
		return result;
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
		InstanceSchema other = (InstanceSchema) obj;
		if (this.attributeList == null) {
			if (other.attributeList != null) {
				return false;
			}
		} else if (!this.attributeList.equals(other.attributeList)) {
			return false;
		}
		if (this.relationName == null) {
			if (other.relationName != null) {
				return false;
			}
		} else if (!this.relationName.equals(other.relationName)) {
			return false;
		}
		return true;
	}

}
