package ai.libs.jaicore.ml.core;

import java.util.ArrayList;

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
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + this.numColumns;
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		return this.numColumns == ((ASimpleInstancesImpl)obj).numColumns;
	}
}
