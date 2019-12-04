package ai.libs.jaicore.ml.core.dataset;

import ai.libs.jaicore.ml.core.filter.sampling.IClusterableInstance;

public abstract class AInstance implements IClusterableInstance {

	private Object label;

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
