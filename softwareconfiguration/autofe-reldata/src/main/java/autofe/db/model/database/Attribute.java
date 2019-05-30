package autofe.db.model.database;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Attribute implements Comparable<Attribute> {

	private String fullName;

	private AttributeType type;

	private boolean isTarget;

	private boolean isPrimaryKey;

	public Attribute(final String name, final AttributeType type) {
		super();
		this.fullName = name;
		this.type = type;
		this.isTarget = false;
		this.isPrimaryKey = false;
	}

	public Attribute(final String name, final AttributeType type, final boolean isTarget) {
		super();
		this.fullName = name;
		this.type = type;
		this.isTarget = isTarget;
		this.isPrimaryKey = false;
	}

	public Attribute(final String name, final AttributeType type, final boolean isTarget, final boolean isPrimaryKey) {
		super();
		this.fullName = name;
		this.type = type;
		this.isTarget = isTarget;
		this.isPrimaryKey = isPrimaryKey;
	}

	public String getFullName() {
		return this.fullName;
	}

	public String getName() {
		String[] split = this.fullName.split("\\.");
		if (split.length != 2) {
			throw new InvalidAttributeNameException("Invalid attribute full name: " + this.fullName);
		}
		return split[1];
	}

	public AttributeType getType() {
		return this.type;
	}

	public void setType(final AttributeType type) {
		this.type = type;
	}

	public boolean isTarget() {
		return this.isTarget;
	}

	public void setTarget(final boolean isTarget) {
		this.isTarget = isTarget;
	}

	public boolean isAggregable() {
		return this.type.isAggregable();
	}

	public boolean isFeature() {
		return (this.type == AttributeType.NUMERIC) || (this.type == AttributeType.TEXT);
	}

	public boolean isPrimaryKey() {
		return this.isPrimaryKey;
	}

	public void setPrimaryKey(final boolean isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
	}

	@Override
	public int compareTo(final Attribute o) {
		return this.fullName.compareTo(o.fullName);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.isPrimaryKey).append(this.isTarget).append(this.fullName).append(this.type).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Attribute)) {
			return false;
		}
		Attribute other = (Attribute) obj;
		return new EqualsBuilder().append(this.isPrimaryKey, other.isPrimaryKey).append(this.isTarget, other.isTarget).append(this.fullName, other.fullName).append(this.type, other.type).isEquals();
	}

	@Override
	public String toString() {
		return "Attribute [fullName=" + this.fullName + ", type=" + this.type + ", isTarget=" + this.isTarget + ", isPrimaryKey=" + this.isPrimaryKey + "]";
	}

}
