package ai.libs.jaicore.ml.core;

import java.util.ArrayList;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import ai.libs.jaicore.ml.interfaces.Instances;

@SuppressWarnings("serial")
public abstract class ASimpleInstancesImpl<I> extends ArrayList<I> implements Instances<I> {

	protected int numColumns = -1;

	public ASimpleInstancesImpl() {
		super();
	}

	public ASimpleInstancesImpl(final int capacity) {
		super(capacity);
	}

	@Override
	public int getNumberOfRows() {
		return this.size();
	}

	@Override
	public int getNumberOfColumns() {
		return this.numColumns;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(super.hashCode()).append(this.numColumns).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof ASimpleInstancesImpl)) {
			return false;
		}
		return this.numColumns == ((ASimpleInstancesImpl<?>)obj).numColumns;
	}
}
