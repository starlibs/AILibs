package ai.libs.jaicore.ml.core.dataset;

import java.util.ArrayList;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

public abstract class ALabeledDataset<I extends ILabeledInstance> extends ArrayList<I> implements ILabeledDataset<I> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1158266286156653852L;

	private ILabeledInstanceSchema schema;

	protected ALabeledDataset(final ILabeledInstanceSchema schema) {
		super();
		this.schema = schema;
	}

	@Override
	public ILabeledInstanceSchema getInstanceSchema() {
		return this.schema;
	}

}
