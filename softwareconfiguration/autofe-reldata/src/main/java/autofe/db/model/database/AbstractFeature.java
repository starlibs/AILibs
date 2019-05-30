package autofe.db.model.database;

import org.apache.commons.lang3.builder.EqualsBuilder;

public abstract class AbstractFeature implements Comparable<AbstractFeature> {

	protected Attribute parent;

	public AbstractFeature(final Attribute parent) {
		super();
		this.parent = parent;
	}

	public abstract String getName();

	public abstract AttributeType getType();

	public Attribute getParent() {
		return this.parent;
	}

	public void setParent(final Attribute parent) {
		this.parent = parent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.parent == null) ? 0 : this.parent.hashCode());
		return result;

	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof AbstractFeature)) {
			return false;
		}
		AbstractFeature other = (AbstractFeature) obj;
		return new EqualsBuilder().append(this.parent, other.parent).isEquals();
	}

	@Override
	public int compareTo(final AbstractFeature o) {
		return this.parent.compareTo(o.parent);
	}

}
