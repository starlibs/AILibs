package ai.libs.jaicore.ml.core.dataset;

import java.io.Serializable;

import ai.libs.jaicore.ml.core.filter.sampling.IClusterableInstance;

public abstract class AInstance implements IClusterableInstance, Serializable {

	private Object label;

	/* just for serialization issues */
	protected AInstance() {
	}

	protected AInstance(final Object label) {
		this.label = label;
	}

	@Override
	public Object getLabel() {
		return this.label;
	}

	@Override
	public void setLabel(final Object label) {
		this.label = label;
	}

	@Override
	public boolean isLabelPresent() {
		return this.label != null;
	}
}
