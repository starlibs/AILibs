package ai.libs.jaicore.ml.core.newdataset;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;

public abstract class AInstance implements IInstance {

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
	public ILabeledInstanceSchema getInstanceSchema() {
		throw new UnsupportedOperationException("Not yet implemented in DenseInstance.");
	}

}
