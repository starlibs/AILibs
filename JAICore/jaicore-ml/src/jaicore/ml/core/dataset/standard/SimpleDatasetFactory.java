package jaicore.ml.core.dataset.standard;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IDatasetFactory;
import jaicore.ml.core.dataset.InstanceSchema;

public class SimpleDatasetFactory implements IDatasetFactory<SimpleInstance> {
	
	@Override
	public IDataset<SimpleInstance> createEmptyDataSet(InstanceSchema schema) {
		return new SimpleDataset(schema);
	}
	
	
}
