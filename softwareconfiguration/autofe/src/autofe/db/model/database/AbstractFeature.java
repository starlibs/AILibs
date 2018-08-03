package autofe.db.model.database;

public abstract class AbstractFeature implements Comparable<AbstractFeature> {

	protected Attribute parent;

	public AbstractFeature(Attribute parent) {
		super();
		this.parent = parent;
	}

	public abstract String getName();
	
	public abstract AttributeType getType();

	public Attribute getParent() {
		return parent;
	}

	public void setParent(Attribute parent) {
		this.parent = parent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
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
		AbstractFeature other = (AbstractFeature) obj;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}

	@Override
	public int compareTo(AbstractFeature o) {
		return this.parent.compareTo(o.parent);
	}

}
