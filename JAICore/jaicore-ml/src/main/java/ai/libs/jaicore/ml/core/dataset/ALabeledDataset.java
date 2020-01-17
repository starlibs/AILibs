package ai.libs.jaicore.ml.core.dataset;

import java.util.ArrayList;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

public abstract class ALabeledDataset<I extends ILabeledInstance> extends ArrayList<I> implements ILabeledDataset<I> {

	private static final long serialVersionUID = 1158266286156653852L;

	private transient ILabeledInstanceSchema schema;

	protected ALabeledDataset(final ILabeledInstanceSchema schema) {
		super();
		this.schema = schema;
	}

	@Override
	public ILabeledInstanceSchema getInstanceSchema() {
		return this.schema;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.schema == null) ? 0 : this.schema.hashCode());
		return result;
	}

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
		ALabeledDataset other = (ALabeledDataset) obj;
		if (this.schema == null) {
			if (other.schema != null) {
				return false;
			}
		} else if (!this.schema.equals(other.schema)) {
			return false;
		}
		return true;
	}
}
