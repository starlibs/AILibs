package ai.libs.jaicore.ml.core.dataset.simple;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

import ai.libs.jaicore.ml.core.dataset.ALabeledDataset;

public class SimpleDataset extends ALabeledDataset<SimpleInstance> {

	/**
	 *
	 */
	private static final long serialVersionUID = -6885612275374708744L;

	public SimpleDataset(final ILabeledInstanceSchema schema) {
		super(schema);
	}

	@Override
	public SimpleDataset createEmptyCopy() throws DatasetCreationException, InterruptedException {
		return new SimpleDataset(this.getInstanceSchema());
	}

	@Override
	public Object[][] getFeatureMatrix() {
		return (Object[][]) this.stream().map(x -> x.getAttributes()).toArray();
	}

	@Override
	public Object[] getLabelVector() {
		return this.stream().map(x -> x.getLabel()).toArray();
	}

}
