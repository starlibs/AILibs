package autofe.db.model;

public class Attribute {

	protected String name;

	protected AttributeType type;

	protected boolean isTarget;

	public Attribute(String name, AttributeType type) {
		super();
		this.name = name;
		this.type = type;
		this.isTarget = false;
	}

	public Attribute(String name, AttributeType type, boolean isTarget) {
		super();
		this.name = name;
		this.type = type;
		this.isTarget = isTarget;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AttributeType getType() {
		return type;
	}

	public void setType(AttributeType type) {
		this.type = type;
	}

	public boolean isTarget() {
		return isTarget;
	}

	public void setTarget(boolean isTarget) {
		this.isTarget = isTarget;
	}

	public boolean isAggregable() {
		return this.type.isAggregable();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isTarget ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Attribute other = (Attribute) obj;
		if (isTarget != other.isTarget)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Attribute [name=" + name + ", type=" + type + ", isTarget=" + isTarget + "]";
	}

}
