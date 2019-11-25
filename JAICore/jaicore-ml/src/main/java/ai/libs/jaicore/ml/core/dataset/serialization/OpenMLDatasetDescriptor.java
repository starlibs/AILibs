package ai.libs.jaicore.ml.core.dataset.serialization;

import org.api4.java.ai.ml.core.dataset.descriptor.IDatasetDescriptor;

public class OpenMLDatasetDescriptor implements IDatasetDescriptor {

	private final int openMLId;


	public OpenMLDatasetDescriptor(final int openMLId) {
		super();
		this.openMLId = openMLId;
	}

	@Override
	public String getDatasetDescription() {
		return "" + this.openMLId;
	}

	public int getOpenMLId() {
		return this.openMLId;
	}
}
