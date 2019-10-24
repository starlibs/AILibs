package ai.libs.jaicore.ml.core.dataset;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

public abstract class AInstance implements ILabeledInstance {

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

}
